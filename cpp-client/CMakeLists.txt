cmake_minimum_required(VERSION 3.12)
project(timelapse_raspberry)
include_directories(./lib)
include_directories(./include)
include_directories(./src)

set(CMAKE_CXX_STANDARD 14)

add_executable(timelapse_raspberry main.cpp)