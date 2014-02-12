#include <iostream>
#include <fstream>
#include <list>
#include <string>
#include "eventlist.h"

/******************************************************************************/
/* Event                                                                      */
/******************************************************************************/


Event::Event(int x) {
  type = x;
}

int Event::getType() {
  return type;
}

string Event::toString() {
  string event = "event";
  return event;
}

/******************************************************************************/
/* Allocation                                                                 */
/******************************************************************************/

Allocation::Allocation(string argklass, unsigned long argtag, unsigned long argcaller, string staticcaller)
  : Event(1) {
  klass = argklass;
  tag = argtag;
  caller = argcaller;
  static_caller = staticcaller;
}

string Allocation::toString() {
  string alloc = 
    "Allocation:" +
    to_string(tag) +
    " class:" +
    klass +
    " caller:" +
    ((static_caller.compare("") != 0) ? static_caller : to_string(caller));
  return alloc;
}

/******************************************************************************/
/* Get Field                                                                  */
/******************************************************************************/

GetField::GetField(string fieldname,
		   string desc,
		   string strcaller,
		   string strcallee,
		   unsigned long argcaller,
		   unsigned long argcallee,
		   unsigned long avalue)
  : Event(2) {
  field = fieldname;
  description = desc;
  static_caller = strcaller;
  static_callee = strcallee;
  caller = argcaller;
  callee = argcallee;
  value = avalue;
}

GetField::~GetField() {
  delete &field;
  delete &description;

    delete &static_caller;

  delete &static_callee;
}


string GetField::toString() {
  string fa = 
    "GetField:" +
    field +
    "(" + description + ")" +
    " caller:" +
    ((static_caller.compare("") != 0) ? static_caller : to_string(caller)+ "\t") +
    " \tcallee:" +
    ((static_callee.compare("") != 0) ? static_callee : to_string(callee)) +
    " \tvalue:" +
    to_string(value);

  return fa;
}

/******************************************************************************/
/* Store Field                                                                */
/******************************************************************************/

StoreField::StoreField(string fieldname,
		       string desc,
		       string strcaller,
		       string strcallee,
		       unsigned long argcaller,
		       unsigned long argcallee,
		       unsigned long avalue,
		       unsigned long oldvalue) :
  Event(3) 
{
  field = fieldname;
  description = desc;
  
  static_caller = strcaller;
  static_callee = strcallee;

  caller = argcaller;
  callee = argcallee;
  value =  avalue;
  old_value = oldvalue;
}

StoreField::~StoreField() {

}


string StoreField::toString() {
  // string fm =
  //   "StoreField:" +
  //   field +
  //   "(" + description + ")" +
  //   " caller:" +
  //   ((static_caller.compare("") != 0) ? static_caller : to_string(caller)+ "\t") +
  //   " \tcallee:" +
  //   ((static_callee.compare("") != 0) ? static_callee : to_string(callee)) +
  //   " \tvalue:" +
  //   ((value != 0) ? to_string(value) : "-") +
  //     " \toldValue:" +
  //   to_string(old_value);

  std::string fm;
  fm.append("storefield: ");
  fm.append(field);
  return fm;
}

/******************************************************************************/
/* Method Call                                                                */
/******************************************************************************/

MethodCall::MethodCall(string met,
		       string desc,
		       string astaticcallee,
		       string astaticcaller,
		       unsigned long aobjcallee,
		       unsigned long aobjcaller,
		       unsigned long *aargs,
		       int aargcount) 
  : Event(4) {
  method = met;
  description = desc;
  staticcallee = astaticcallee;
  staticcaller = astaticcaller;
  objcallee = aobjcallee;
  objcaller = aobjcaller;
  args = aargs;
  arg_count = aargcount;
}


MethodCall::~MethodCall() {
  delete args;
}


string MethodCall::toString() {
  
  string args_str =  " args: ";
    for (int i=0;i<arg_count;i++) {
      args_str.append(to_string(args[i]));
    }

  string mc = 
    "Method Enter: " +
    method +
    " " +
    description +
    " callee:" +
    (!(staticcallee.empty()) ? "<static:" + staticcallee + ">" : to_string(objcallee)) +
    " caller:" +
    (!(staticcaller.empty()) ? "<static:" + staticcaller + ">" : ((objcaller == 0) ? "-" : to_string(objcaller)))

    ;

  return mc + args_str;
}

/******************************************************************************/
/* Deallocation                                                               */
/******************************************************************************/

Deallocation::Deallocation(unsigned long argtarget) 
  : Event(5) {
  target = argtarget;
}

string Deallocation::toString() {
  string dealloc = "Deallocation: "+ to_string(target);
  return dealloc;
}

/******************************************************************************/
/* Returned                                                                   */
/******************************************************************************/

Returned::Returned(string method,
		   string description,
		   string astaticcallee,
		   string astaticcaller,
		   unsigned long acaller,
		   unsigned long acallee,
		   unsigned long atarget,
		   unsigned long *aoutobjs,
		   int acounter) 
  : Event(6)
{
  met = method;
  desc = description;
  staticcallee = astaticcallee;
  staticcaller = astaticcaller;
  caller = acaller;
  callee = acallee;
  target = atarget;
  outobjs = aoutobjs;
  counter = acounter;
}

Returned::~Returned() {
  delete outobjs;
}

string Returned::toString() {
  string args_str =  " Out of scope: ";
    for (int i=0;i<counter;i++) {
      args_str.append(to_string(outobjs[i]) + " ");
    }
  string mc = 
    "Method exit: " +
    met +
    " " +
    desc +
    " callee:" +
    (!(staticcallee.empty()) ? "<static:" + staticcallee + ">" : to_string(callee)) +
    " caller:" +
    (!(staticcaller.empty()) ? "<static:" + staticcaller + ">" : ((caller == 0) ? "-" : to_string(caller))) +
    " Return Value:" +
    ((target != 0) ? to_string(target) : "-")
    ;
  return mc + args_str;
}

/******************************************************************************/
/* Stored Variable                                                            */
/******************************************************************************/

StoredVar::StoredVar(string amet,
		     string adesc,
		     string astatic_callee,
		     unsigned long acallee,
		     unsigned long astored,
		     unsigned long aold)
  : Event(7) {
  met = amet;
  desc = adesc;
  static_callee = astatic_callee;
  callee = acallee;
  stored = astored;
  old = aold;
}
string StoredVar::toString() {
    string mc = 
      "Stored var: " + 
      met +
      " " +
      desc +
      " obj:" +
      to_string(stored) +
      " callee:" +
      (!(static_callee.empty()) ? "<static:" + static_callee + ">" : to_string(callee))
      ;

    string overwritten = old != 0 ? "overwritten: " + to_string(old) : "";
  return mc+overwritten;

}

/******************************************************************************/
/* list<Event *> functions                                                    */
/******************************************************************************/

void printList(list<Event*> list, int outputMethod, const char *filename) {
  if (outputMethod == 0) {
    for (std::list<Event*>::iterator it=list.begin(); it != list.end(); ++it) {
      cout << (*it)->toString() << "\n";
      //cout << (*it)->getType() << endl;
    }
  }

  if (outputMethod == 1) {
    ofstream myfile;
    myfile.open (filename);
    for (std::list<Event*>::iterator it=list.begin(); it != list.end(); ++it) {
      myfile << (*it)->toString() << "\n";
      // cout << (*it)->getType();
    }
    myfile.close();
  }
}
