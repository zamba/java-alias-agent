#include "eventlist.h"
#include<iostream>
#include<string>

using namespace std;

bool test_allocation(list<Event *> events)
{
  return false;
}

/******************************************************************************/
/* Field tests                                                                */
/******************************************************************************/

bool field_store(list<Event *> events)
{
  return false;
}

bool field_load(list<Event *> events)
{
  return false;
}

bool field_overwrite(list<Event *> events)
{
  return false;
}

/******************************************************************************/
/* Method tests                                                               */
/******************************************************************************/

bool method_call(list<Event *> events)
{
  return false;
}

bool passed(list<Event *> events)
{
  return false;
}

bool returned(list<Event *> events)
{
  return false;
}

/******************************************************************************/
/* Variable tests                                                             */
/******************************************************************************/

bool variable_store(list<Event *> events)
{
  return false;
}

bool variable_scope(list<Event *> events)
{
  return false;
}

bool variable_overwrite(list<Event *> events)
{
  return false;
}






void test_fun(int test,list<Event *> events)
{
  bool result;
  switch (test) 
    {
    case 1:
      result = test_allocation(events);
      break;
    case 2:
      result = field_store(events);
      break;
    case 3:
      result = field_load(events);
      break;
    case 4:
      result = field_overwrite(events);
      break;

    case 5:
      result = method_call(events);
      break;
    case 6:
      result = passed(events);
      break;
    case 7:
      result = returned(events);
      break;

    case 8:
      result = variable_store(events);
      break;
    case 9:
      result = variable_scope(events);
      break;
    case 10:
      result = variable_overwrite(events);
      break;
    default:
      return;
    }
  string passed = "Passed";
  string failed = "Failed";
  cout << "Test nr: " << to_string(test)  << (result ? passed : failed) << endl; 
}
