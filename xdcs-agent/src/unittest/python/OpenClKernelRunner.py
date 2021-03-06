import unittest

import numpy as np
import pyopencl as cl

import xdcs.computing.OpenClKernelRunner as Compiler
from xdcs.kernel import KernelArgument


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

        arg1 = KernelArgument('arg1', 'obj_id_1', 1, KernelArgument.Direction.IN, KernelArgument.Type.SIMPLE)
        arg1.value = np.uint16(n)
        arg2 = KernelArgument('arg2', 'obj_id_2', 2, KernelArgument.Direction.IN, KernelArgument.Type.SIMPLE)
        arg2.value = np.uint16(m)
        arg3 = KernelArgument('arg3', 'obj_id_3', 3, KernelArgument.Direction.IN, KernelArgument.Type.SIMPLE)
        arg3.value = np.uint16(p)
        arg4 = KernelArgument('arg4', 'obj_id_4', 4, KernelArgument.Direction.IN, KernelArgument.Type.POINTER)
        arg4.value = a_buf
        arg5 = KernelArgument('arg5', 'obj_id_5', 5, KernelArgument.Direction.IN, KernelArgument.Type.POINTER)
        arg5.value = b_buf
        arg6 = KernelArgument('arg6', 'obj_id_6', 6, KernelArgument.Direction.OUT, KernelArgument.Type.POINTER)
        arg6.value = c_buf

        arg_list = [arg1.value, arg2.value, arg3.value, arg4.value, arg5.value, arg6.value]

        queue = Compiler.OpenClKernelRunner.run(program, func_name, ctx, c.shape, None, arg_list)

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
