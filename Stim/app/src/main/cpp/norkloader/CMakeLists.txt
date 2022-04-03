cmake_minimum_required(VERSION 3.0)
project(norkloader)

add_executable(${PROJECT_NAME} src/NorKyst8000FimexDataLoader.cpp)
target_link_libraries(${PROJECT_NAME} PUBLIC fimex)