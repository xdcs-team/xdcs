#!/usr/bin/env bash

DOCKER_NVIDIA_DEVICES="--device /dev/nvidia0 --device /dev/nvidiactl --device /dev/nvidia-uvm"
#docker run -ti $DOCKER_NVIDIA_DEVICES tleyden5iwx/ubuntu-cuda /bin/bash


#docker run -it $DOCKER_NVIDIA_DEVICES $(docker build -q . -f Dockerfile.3)
docker run -it --runtime=nvidia $(docker build -q . -f Dockerfile.3)
