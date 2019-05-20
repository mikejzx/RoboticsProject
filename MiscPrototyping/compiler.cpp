/*
    Simple program that will point to the Windows
    C# compiler to make life slightly easier.
*/

#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <string.h>


// Application entry point.
int main(const int argc, const char* argv[]) {
	if (argc < 2) {
		std::cout << "You must specify a file which you wish to compile" << std::endl;
	    return -1;
    }

	std::string file = argv[1];
    std::string command = std::string("C:\\Windows\\Microsoft.NET\\Framework\\v4.0.30319\\csc.exe ").append(file);


    std::cout << "Compiling " << file << "..." << std::endl;
    system(command.c_str());

    return 0;
}
