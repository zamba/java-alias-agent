#ifndef EVENTLIST_H
#define EVENTLIST_H
#include <string>
#include <list>

using namespace std;

/******************************************************************************/
/* Event                                                                      */
/******************************************************************************/

class Event {
private:
  int type;
 public:
  Event(int x);
  int getType();
  virtual string toString();
};

/******************************************************************************/
/* Allocation                                                                 */
/******************************************************************************/

class Allocation : public Event {
 private:
  string klass;
  unsigned long tag;
  unsigned long caller;
  string static_caller;
 public:
  Allocation(string argklass,
	     unsigned long tag,
	     unsigned long caller,
	     string staticcaller);
  string toString();
};

/******************************************************************************/
/* Get Field                                                                  */
/******************************************************************************/

class GetField : public Event {
 private:
  string field;
  string description;
  
  string static_caller;
  string static_callee;

  unsigned long caller;
  unsigned long callee;
  unsigned long value;

 public:
  GetField(string fieldname,
	   string desc,
	   string strcaller,
	   string strcallee,
	   unsigned long argcaller,
	   unsigned long argcallee,
	   unsigned long value);
  ~GetField();
  string toString();
};

/******************************************************************************/
/* Store Field                                                                */
/******************************************************************************/

class StoreField : public Event {
 private:
  string field;
  string description;
  
  string static_caller;
  string static_callee;

  unsigned long caller;
  unsigned long callee;
  unsigned long value;
  unsigned long old_value;
 public:
  StoreField(string fieldname,
	     string desc,
	     string strcaller,
	     string strcallee,
	     unsigned long argcaller,
	     unsigned long argcallee,
	     unsigned long value,
	     unsigned long oldvalue);
  ~StoreField();
  string toString();
};

/******************************************************************************/
/* Method Call                                                                */
/******************************************************************************/

class MethodCall : public Event {
 private:
  string method;
  string description;
  string staticcallee;
  string staticcaller;
  unsigned long objcallee;
  unsigned long objcaller;
  unsigned long *args;
  int arg_count;
 public:
  MethodCall(string met,
	     string desc,
	     string astaticcallee,
	     string astaticcaller,
	     unsigned long aobjcallee,
	     unsigned long aobjcaller,
	     unsigned long *args,
	     int argcount);
  ~MethodCall();
  string toString();
};

/******************************************************************************/
/* Deallocation                                                               */
/******************************************************************************/

class Deallocation : public Event {
 private:
  unsigned long target;
 public:
  Deallocation(unsigned long argtarget);
  string toString();
};

/******************************************************************************/
/* Returned                                                                   */
/******************************************************************************/

class Returned : public Event {
 private:
  string met;
  string desc;
  string staticcallee;
  string staticcaller;
  unsigned long callee;
  unsigned long caller;
  unsigned long target;
  unsigned long *outobjs;
  int counter;
  
 public:
  Returned(string method,
	   string description,
	   string staticcallee,
	   string staticcaller,
	   unsigned long caller,
	   unsigned long callee,
	   unsigned long target,
	   unsigned long *outobjs,
	   int counter);
  ~Returned();
	   
  string toString();
};

/******************************************************************************/
/* Overwritten                                                              */
/******************************************************************************/

class Overwrite : public Event {
 private:
  string field;
  unsigned long caller;
  unsigned long callee;
  unsigned long stored;
 public:
  Overwrite(string argfield, unsigned long argcaller, unsigned long argcallee, unsigned long argstored);
  string toString();
};

/******************************************************************************/
/* StoredVar                                                              */
/******************************************************************************/

class StoredVar : public Event {
 private:
  string met;
  string desc;
  string static_callee;
  unsigned long callee;
  unsigned long stored;
  unsigned long old;
 public:
  StoredVar(string met,
	    string desc,
	    string static_callee,
	    unsigned long callee,
	    unsigned long stored,
	    unsigned long old);
  string toString();
};

// 0 - stdout
// 1 - to file(output)
void printList(list<Event*> list, int outputMethod, const char *filename);

#endif
