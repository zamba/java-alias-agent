#include "eventlist.h"
#include<iostream>
#include<string>

using namespace std;

bool test_allocation(list<Event *> list)
{
  int nr = 0;
  for (std::list<Event*>::iterator it=list.begin(); it != list.end(); ++it) {
    if ((*it)->getType() == 1)
      nr++;
  }
  if (nr == 6)
    return true;

  cout << "Expected:6 Found:" << to_string(nr) << endl;
  return false;
}

/******************************************************************************/
/* Field tests                                                                */
/******************************************************************************/

bool field_store(list<Event *> list)
{
  int nr = 0;
  for (std::list<Event*>::iterator it=list.begin(); it != list.end(); ++it) {
    if ((*it)->getType() == 3)
      nr++;
  }
  if (nr == 5)
    return true;

  cout << "Expected:5 Found:" << to_string(nr) << endl;
  return false;
}

bool field_load(list<Event *> list)
{
  int nr = 0;
  for (std::list<Event*>::iterator it=list.begin(); it != list.end(); ++it) {
    if ((*it)->getType() == 2)
      nr++;
  }
  if (nr == 5)
    return true;

  cout << "Expected:5 Found:" << to_string(nr) << endl;
  return false;
}


/******************************************************************************/
/* Method tests                                                               */
/******************************************************************************/

bool method_call(list<Event *> list)
{
  int nr = 0;
  for (std::list<Event*>::iterator it=list.begin(); it != list.end(); ++it) {
    if ((*it)->getType() == 4)
      nr++;
  }
  if (nr == 12)
    return true;

  cout << "Expected:12 Found:" << to_string(nr) << endl;
  return false;
}


bool returned(list<Event *> list)
{
  int nr = 0;
  for (std::list<Event*>::iterator it=list.begin(); it != list.end(); ++it) {
    if ((*it)->getType() == 6)
      nr++;
  }

  if (nr == 12)
    return true;

  cout << "Expected:12 Found:" << to_string(nr) << endl;
  return false;
}

/******************************************************************************/
/* Variable tests                                                             */
/******************************************************************************/

bool variable_store(list<Event *> list)
{
  int nr = 0;
  for (std::list<Event*>::iterator it=list.begin(); it != list.end(); ++it) {
    if ((*it)->getType() == 7)
      nr++;
  }
  if (nr == 4)
    return true;
    
  cout << "Expected:4 Found:" << to_string(nr) << endl;
  return false;
}




void test_fun(int test,list<Event *> events)
{
  bool result = false;
  string k;
  switch (test) 
    {
    case 1:
      result = test_allocation(events);
      k = "allocation";
      break;
    case 2:
      result = field_store(events);
      k = "fieldstore";
      break;
    case 3:
      result = field_load(events);
      k = "fieldload";
      break;
    case 4:
      result = method_call(events);
      k = "methodenter";
      break;
    case 5:
      result = returned(events);
      k = "methodexit";
      break;
    case 6:
      result = variable_store(events);
      k = "varstore";
      break;

    default:
      return;
    }
  string passed = "Passed";
  string failed = "Failed";

  cout << "Test nr(" << k  << "): \t" << to_string(test) << " "  << (result ? passed : failed) << endl; 
}
