import unittest

import numpy as np
import pyopencl as cl

import xdcs.computing.OpenClKernelRunner as Compiler
from xdcs.parameters import KernelParameters
from xdcs.parameters import KernelParameter
from xdcs.parameters import ParameterDirection
from xdcs.parameters import ParameterType


class KernelTest(unittest.TestCase):

    @unittest.skip("No graphic card on Travis")
    def test_func_run_simple_program(self):
        func_name = 'multiply'
        program = """
                __kernel void multiply(ushort n,
                ushort m, ushort p, __global float *a,
                __global float *b, __global float *c)
                {
                  int gid = get_global_id(0);
                  c[gid] = 0.0f;
                  int rowC = gid/p;
                  int colC = gid%p;
                  __global float *pA = &a[rowC*m];
                  __global float *pB = &b[colC];
                  for(int k=0; k<m; k++)
                  {
                     pB = &b[colC+k*p];
                     c[gid] += (*(pA++))*(*pB);
                  }
                }
        """

        (n, m, p) = (3, 4, 5)

        a = np.random.randint(2, size=(n * m))
        b = np.random.randint(2, size=(m * p))
        c = np.zeros((n * p), dtype=np.float32)

        a = a.astype(np.float32)
        b = b.astype(np.float32)

        ctx = cl.create_some_context()

        mf = cl.mem_flags
        a_buf = cl.Buffer(ctx, mf.READ_ONLY | mf.COPY_HOST_PTR, hostbuf=a)
        b_buf = cl.Buffer(ctx, mf.READ_ONLY | mf.COPY_HOST_PTR, hostbuf=b)
        c_buf = cl.Buffer(ctx, mf.WRITE_ONLY, c.nbytes)

        param2 = KernelParameter(ParameterDirection.IN, np.uint16(n), 1, ParameterType.SIMPLE, 'param2')
        param3 = KernelParameter(ParameterDirection.IN, np.uint16(m), 2, ParameterType.SIMPLE, 'param3')
        param4 = KernelParameter(ParameterDirection.IN, np.uint16(p), 3, ParameterType.SIMPLE, 'param4')
        param5 = KernelParameter(ParameterDirection.IN, a_buf, 4, ParameterType.POINTER, 'param5')
        param6 = KernelParameter(ParameterDirection.IN, b_buf, 5, ParameterType.POINTER, 'param6')
        param7 = KernelParameter(ParameterDirection.IN, c_buf, 6, ParameterType.POINTER, 'param7')

        param_list = list()
        param_list.extend((param2, param3, param4, param5, param6, param7))
        parameters = KernelParameters(*param_list)

        queue = Compiler.OpenClKernelRunner.run(program, func_name, c.shape, parameters, ctx)

        a_mul_b = np.empty_like(c)
        cl.enqueue_copy(queue, a_mul_b, c_buf)

        print("matrix A:")
        print(a.reshape(n, m))
        print("matrix B:")
        print(b.reshape(m, p))
        print("multiplied A*B:")
        print(a_mul_b.reshape(n, p))


if __name__ == '__main__':
    unittest.main()
