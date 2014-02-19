#include "gzstream.h"
#include <iostream> //cout cerr cin clog
#include <fstream>
#include <stdlib.h>

int main(int argc, const char* argv[]) {
    ogzstream  out( argv[1]);
    if ( ! out.good()) {
        std::cerr << "ERROR: Opening file `" << argv[2] << "' failed.\n";
	return EXIT_FAILURE;
    }
    for (int i=0;i < 1000;i++) {
      out << "writing a line lolollollolllllllo\n";
    }

    out.close();
    if ( ! out.good()) {
        std::cerr << "ERROR: Writing file `" << argv[2] << "' failed.\n";
	return EXIT_FAILURE;
    }
}
