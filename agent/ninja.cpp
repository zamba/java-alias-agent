#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <iostream> // cout

using namespace std;

int main( int argc, const char* argv[] )
{
	if (argc == 2) {
	  char str[128];
	  strcpy(str,"java -agentpath:./testagent.so ");
	  strcat (str,argv[1]);
	  //printf("%s \n",str);
	  system(str);
	}
	else {
	  printf("Usage: ninja <main class>\n");
	}
}
