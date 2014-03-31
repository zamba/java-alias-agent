#include <istream>
#include <iostream>
#include <fstream>

using namespace std;

int main(int argc, char* argv[]) { 
  char current[256];
  ifstream infile;

  if (argc < 2) {
    printf("wrong usage\n");
    return -1;
  }
  infile.open (argv[1]);
  while(!infile.eof()) {
    infile.getline(current,256);
    cout << current << "\n";
  }
  infile.close();
  return 0;
}
