package pl.edu.agh.xdcs.restapi.mapper;

import pl.edu.agh.xdcs.api.CUDADevice;
import pl.edu.agh.xdcs.restapi.model.AdditionalPropertyDto;
import pl.edu.agh.xdcs.restapi.model.CUDADeviceDto;

import java.util.List;

/**
 * @author Krystian Życiński
 */
public class CUDADeviceMapper {

    public CUDADeviceDto toRestEntity(CUDADevice cudaDevice) {
        CUDADeviceDto cudaDeviceDto = new CUDADeviceDto();
        cudaDeviceDto.setName(cudaDevice.getName());
        cudaDeviceDto.setFreeMemory(cudaDevice.getFreeMemory());
        cudaDeviceDto.setTotalMemory(cudaDevice.getTotalMemory());
        cudaDeviceDto.setClockRate(cudaDevice.getClockRate());
        cudaDeviceDto.setIntegrated(cudaDevice.getIntegrated());
        cudaDeviceDto.setComputeCapability(cudaDevice.getComputeCapabilityMajor());
        cudaDeviceDto.setAdditionalProperties(getAdditionalProperties(cudaDevice));
        return cudaDeviceDto;
    }

    private List<AdditionalPropertyDto> getAdditionalProperties(CUDADevice cudaDevice) {
        return null;
    }
}
