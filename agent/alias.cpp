#include <jvmti.h>
#include <jni.h>
#include <string.h> //strstr(), strcmp(), memset()
#include "NativeInterface.h"
#include "eventlist.h"
#include <iostream> // cout
#include "test.h"

using namespace std;

/******************************************************************************/
/* Settings                                                                   */
/******************************************************************************/

// set variables will be recorded
bool alloc=true;
bool methodEnter=true;
bool getField = true;
bool storeField = true;
bool returns = true;
bool storeVar = true;

bool test = false;
int testnr = 0;

const char *segment = "/home/erik/java-alias-agent/agent";


// export method = 0 print recorded info to STDOUT
//                 1 write recorded info to filename
int export_method = 0;
const char *filename = "output";


/******************************************************************************/
/* Global Data                                                                */
/******************************************************************************/

// global ref to jvmti enviroment
jvmtiEnv *g_jvmti = NULL;

// global ref to class and methodID for instrumenting
jclass g_cls = NULL;
jmethodID g_mid = 0;

// indicates JVM initialization
bool g_init = false;

// tags 1 to n
jlong g_objectid = 1;

// list with recorded events
list<Event *> eventlist;

/******************************************************************************/
/* Monitor management                                                         */
/******************************************************************************/

static jrawMonitorID g_lock;

/*
  Enter a critical section by doing a JVMTI Raw Monitor Enter
*/
static void enter_critical_section() {
  jvmtiError error;
  error = g_jvmti->RawMonitorEnter(g_lock);
}


/*
  Exit a critical section by doing a JVMTI Raw Monitor Exit
*/
static void exit_critical_section() {
  jvmtiError error;
  error = g_jvmti->RawMonitorExit(g_lock);
}

/*
  returns a string(c++ string) copied from str(java string)
*/
string toCPS(JNIEnv *env,
	     jstring str) {
  if (str == NULL) {
    string strstr = "";
    return strstr;
  }
  const char *c_str = env->GetStringUTFChars(str, NULL);
  string result = c_str;
  env->ReleaseStringUTFChars(str, c_str);
  return result;
}

/*
  returns obj's tag, or 0 if obj == NULL
*/
jlong get_tag(jobject obj) {
  jlong tag = 0;
  if (obj){
    g_jvmti->GetTag(obj,&tag);
  }
  return tag;
}


/******************************************************************************/
/* Event Callbacks                                                            */
/******************************************************************************/


/* 
   TODO:
*/
JNIEXPORT void JNICALL Java_NativeInterface_storeVar
(JNIEnv *env,
 jclass native_interface,
 jobject stored,
 jobject old,
 jstring method,
 jstring desc,
 jstring static_callee,
 jobject callee,
 jthread thread) 
{
  if (storeVar) {
    enter_critical_section(); {
      jlong stored_tag = get_tag(stored);
      jlong callee_tag = get_tag(callee);
      jlong old_tag = get_tag(old);
      string cp_method = toCPS(env,method);
      string cp_desc = toCPS(env,desc);
      string cp_callee;
      if (callee_tag == 0) {
	cp_callee = toCPS(env,static_callee);
      }
      StoredVar *event = new StoredVar(cp_method,
				       cp_desc,
				       cp_callee,
				       callee_tag,
				       stored_tag,
				       old_tag);
      eventlist.push_back(event);
    } exit_critical_section();
  }
}


/* 
   TODO:
   * make use of toCPS()
   * merge with loadField()
   */
JNIEXPORT void JNICALL Java_NativeInterface_storeField
(JNIEnv *env,
 jclass native_interface,
 jobject obj,
 jobject value,
 jobject old_value,
 jstring owner,
 jstring name,
 jstring desc,
 jstring static_caller,
 jobject caller,
 jthread thread) 
{
  if (storeField) {
    enter_critical_section(); {
      jlong old_value_tag = get_tag(old_value);
      jlong callee_tag = get_tag(obj);
      jlong caller_tag = get_tag(caller);
      jlong value_tag = get_tag(value);

      const char* c_owner;
      const char* c_name;
      const char* c_desc;
      const char* c_caller;
      c_owner = env->GetStringUTFChars(owner, NULL);
      c_name = env->GetStringUTFChars(name, NULL);
      c_desc = env->GetStringUTFChars(desc, NULL);

      string cp_name = c_name;
      string cp_desc = c_desc;
      string cp_caller;
      string cp_callee;
      if (callee_tag == 0) {
	cp_callee = c_owner;
      }

      if (static_caller != NULL) {
	c_caller = env->GetStringUTFChars(static_caller, NULL);
	cp_caller = c_caller;
      }
      StoreField *event = new StoreField(cp_name,
					 cp_desc,
					 cp_caller,
					 cp_callee,
					 caller_tag,
					 callee_tag,
					 value_tag,
					 old_value_tag);
      eventlist.push_back(event);
      if (static_caller != NULL) {
	env->ReleaseStringUTFChars(static_caller, c_caller);
      }

      env->ReleaseStringUTFChars(owner, c_owner);
      env->ReleaseStringUTFChars(name, c_name);
      env->ReleaseStringUTFChars(desc, c_desc);
    } exit_critical_section(); 
  }
}


/* 
   TODO:
   * make use of toCPS()
   * merge with storeField()
   */
JNIEXPORT void JNICALL Java_NativeInterface_loadField
(JNIEnv *env,
 jclass native_interface,
 jobject obj,
 jobject value,
 jstring owner,
 jstring name,
 jstring desc,
 jstring static_caller,
 jobject caller,
 jthread thread)
{
  if (getField) {
    enter_critical_section(); {
      const char* c_owner;
      const char* c_name;
      const char* c_desc;
      const char* c_caller;
      c_owner = env->GetStringUTFChars(owner, NULL);
      c_name = env->GetStringUTFChars(name, NULL);
      c_desc = env->GetStringUTFChars(desc, NULL);

      jlong caller_tag = 0;
      jlong callee_tag = 0;
      jlong value_tag = 0;
      if (caller!=NULL) {
	g_jvmti->GetTag(caller,&caller_tag);
      }
      if (obj!=NULL) {
	g_jvmti->GetTag(obj,&callee_tag);
      }

      if (value!=NULL) {
	g_jvmti->GetTag(value,&value_tag);
      }

      string cp_name = c_name;
      string cp_desc = c_desc;
      string cp_caller;
      string cp_callee;
      if (callee_tag == 0) {
	cp_callee = c_owner;
      }

      if (static_caller != NULL) {
	c_caller = env->GetStringUTFChars(static_caller, NULL);
	cp_caller = c_caller;
      }
      GetField *event = new GetField(cp_name,
				     cp_desc,
				     cp_caller,
				     cp_callee,
				     caller_tag,
				     callee_tag,
				     value_tag);
      eventlist.push_back(event);
      if (static_caller != NULL) {
	env->ReleaseStringUTFChars(static_caller, c_caller);
      }

      env->ReleaseStringUTFChars(owner, c_owner);
      env->ReleaseStringUTFChars(name, c_name);
      env->ReleaseStringUTFChars(desc, c_desc);
    }exit_critical_section(); 
  }
}


/* 
   TODO:
   * make use of toCPS() and get_tag()
   * merge with storeField()
   * make shorter
   */
JNIEXPORT void JNICALL Java_NativeInterface_methodEnter
(JNIEnv *env,
 jclass nativeinterfacecls,
 jstring met,
 jstring desc,
 jstring staticcallee,
 jobject callee,
 jobjectArray args,
 jthread thread)
{
  enter_critical_section(); {
    
    jlong tag_currentthis = 0;
    jlong tag_caller = 0;
    const char* cmet;
    const char* cdes;
    if (callee) {
      g_jvmti->GetTag(callee, &tag_currentthis);
    }

    // get methodname
    cmet = env->GetStringUTFChars(met, NULL);
    string methodname = string(cmet);
    
    cdes = env->GetStringUTFChars(desc, NULL);
    string methoddesc = string(cdes);

    const char* cstaticcallee;
    string strstaticcallee = "";
    if (staticcallee) {
      cstaticcallee = env->GetStringUTFChars(staticcallee, NULL);
      strstaticcallee = string(cstaticcallee);
    }


    string staticcaller;

    jvmtiFrameInfo *frame;
    g_jvmti->Allocate(3*sizeof(jvmtiFrameInfo),(unsigned char**)&frame);
    jint count;
    g_jvmti->GetStackTrace(thread,1,2,frame,&count);
    if (count > 1) {

      char *methodName2 = NULL;
      g_jvmti->GetMethodName(frame[1].method, &methodName2,NULL, NULL);


      jint access_flags = 0;
      g_jvmti->GetMethodModifiers(frame[1].method,&access_flags);
      //printf("calling met: %s access flags: %d \n",methodName2, access_flags);

      if (access_flags >= 8) {
  	jclass declaring_class;
  	char *source_name;
  	jvmtiError error = g_jvmti->GetMethodDeclaringClass(frame[1].method,&declaring_class);
  	if (error != JVMTI_ERROR_NONE) {
  	  printf("errrrrrrror");
  	}
  	error = g_jvmti->GetClassSignature(declaring_class,&source_name,NULL);
  	if (error != JVMTI_ERROR_NONE) {
  	  printf("errrrrrrror");
  	}
  	strcat(source_name,methodName2);
  	staticcaller = source_name;
  	g_jvmti->Deallocate((unsigned char *)source_name);
      }
      else {
  	jobject callerobj = NULL;
  	jvmtiError error = g_jvmti->GetLocalObject(thread,2,0,&callerobj);
  	if (callerobj != NULL) {
  	  error = g_jvmti->GetTag(callerobj,&tag_caller);
  	  if (error != JVMTI_ERROR_NONE) {
  	    printf("errrrrrrror %d \n",error);
  	  }
  	}
      }
    }

    int argcount = 0;
    unsigned long *argtags = NULL;
    if (args) {
      argcount = env->GetArrayLength(args);
    }
    if (argcount > 0) {
      argtags = new unsigned long[argcount];
      for (int i=0;i < argcount;i++){
  	jobject current = env->GetObjectArrayElement((jobjectArray)args, i);
  	jlong current_tag = 0;
  	g_jvmti->GetTag(current,&current_tag);
  	if (current_tag > 0) {
  	  argtags[i] = current_tag;
  	}
  	else {
  	  /* generate new event */
  	  g_jvmti->SetTag(current,g_objectid++);
  	  argtags[i] = g_objectid - 1;
  	  if (alloc) {
  	    Allocation *event = new Allocation ("N/A",argtags[i],tag_caller,"-");
  	    eventlist.push_back(event);
  	  }
  	}
      }
    }
    if (methodEnter){
      MethodCall *event = new MethodCall (methodname,
  					  methoddesc,
  					  strstaticcallee,
  					  staticcaller,
  					  tag_currentthis,
  					  tag_caller,
  					  argtags,
  					  argcount);

      eventlist.push_back(event);
    }

    // Release Strings
    env->ReleaseStringUTFChars(met, cmet);
    env->ReleaseStringUTFChars(desc, cdes);

    if (staticcallee) {
      env->ReleaseStringUTFChars(staticcallee, cstaticcallee);
    }
  }exit_critical_section();
}


/* 
   TODO:
   * make use of toCPS() and get_tag()
   * make shorter
   */
JNIEXPORT void JNICALL Java_NativeInterface_methodExit
(JNIEnv *env,
 jclass nativeinterfacecls,
 jobject returned,
 jstring met,
 jstring desc,
 jstring staticcallee,
 jobject callee,
 jobjectArray outOfScopes,
 jthread thread ) 
{

  if (returns) {
    enter_critical_section(); {
      jlong tag_callee = 0;
      jlong tag_caller = 0;
      jlong tag_returned = 0;
      string cp_staticcallee;
      string cp_staticcaller;

      // Get method/description from java strings
      string method = toCPS(env,met);
      string description = toCPS(env,desc);
    
      // Get returned object's tag
      if (returned) {
	g_jvmti->GetTag(returned, &tag_returned);
      }

      // Get Callee's tag
      if (callee) {
	g_jvmti->GetTag(callee, &tag_callee);
      }
      
      // if tag was unavailable, Get callee's class
      if (tag_callee == 0) {;
	cp_staticcallee = toCPS(env, staticcallee);
      }

      jvmtiFrameInfo *frame;
      g_jvmti->Allocate(3*sizeof(jvmtiFrameInfo),(unsigned char**)&frame);

      jint count;
      g_jvmti->GetStackTrace(thread,1,2,frame,&count);
      if (count > 1) {

	char *methodName2 = NULL;
	g_jvmti->GetMethodName(frame[1].method, &methodName2,NULL, NULL);


	jint access_flags = 0;
	g_jvmti->GetMethodModifiers(frame[1].method,&access_flags);
	//printf("calling met: %s access flags: %d \n",methodName2, access_flags);
	// flags == 9
	if (access_flags >= 8) {
	  jclass declaring_class;
	  char *source_name;
	  jvmtiError error = g_jvmti->GetMethodDeclaringClass(frame[1].method,&declaring_class);
	  if (error != JVMTI_ERROR_NONE) {
	    printf("errrrrrrror");
	  }
	  error = g_jvmti->GetClassSignature(declaring_class,&source_name,NULL);
	  if (error != JVMTI_ERROR_NONE) {
	    printf("errrrrrrror");
	  }
	  strcat(source_name,methodName2);
	  cp_staticcaller = source_name;
	  g_jvmti->Deallocate((unsigned char *)source_name);
	}
	else {
	  jobject callerobj = NULL;
	  jvmtiError error = g_jvmti->GetLocalObject(thread,2,0,&callerobj);
	  if (callerobj != NULL) {
	    error = g_jvmti->GetTag(callerobj,&tag_caller);
	    if (error != JVMTI_ERROR_NONE) {
	      printf("errrrrrrror %d \n",error);
	    }
	  }
	}
      }

      unsigned long *outobjs = NULL;
      int objcounter = 0;
      if (outOfScopes) {
	objcounter = env->GetArrayLength(outOfScopes);
      }
      if (objcounter > 0) {
	outobjs = new unsigned long[objcounter];
	for (int i=0;i < objcounter;i++){
	  jobject current = env->GetObjectArrayElement((jobjectArray)outOfScopes, i);
	  jlong current_tag = 0;
	  g_jvmti->GetTag(current,&current_tag);
	  if (current_tag > 0) {
	    outobjs[i] = current_tag;
	  }
	}
      }

      Returned *event = new Returned(method,
				     description,
				     cp_staticcallee,
				     cp_staticcaller,
				     tag_caller,
				     tag_callee,
				     tag_returned,
				     outobjs,
				     objcounter);
      eventlist.push_back(event);

      g_jvmti->Deallocate((unsigned char*)frame);

    } exit_critical_section();
  }

}


/* 
   TODO:
   */
JNIEXPORT void JNICALL Java_NativeInterface_newObj
(JNIEnv *env,
 jclass nativeinterface,
 jstring desc,
 jobject stored,
 jstring staticcaller,
 jobject caller,
 jobject thread) 
{
  enter_critical_section(); {
    jlong stored_tag = get_tag(stored);
    if (stored_tag == 0) {
      g_jvmti->SetTag(stored,g_objectid++);
      stored_tag = g_objectid - 1;
    }

    if (alloc) {
      string type = toCPS(env,desc);
      string caller_str = toCPS(env,staticcaller);
      jlong caller_tag = get_tag(caller);

      Allocation *event = new Allocation (type,stored_tag,caller_tag,caller_str);
      eventlist.push_back(event);
    }
  }exit_critical_section();
}


/* 
   Catches event when the gc frees a tagged object
*/
void JNICALL
cbObjectFree(jvmtiEnv *jvmti_env,
	     jlong tag) {
  Deallocation *event = new Deallocation (tag);
  eventlist.push_back(event);
}


/******************************************************************************/
/* System Callbacks                                                           */
/******************************************************************************/


/*
  Sent by the VM when a classes is being loaded into the VM

  Transforms loaded classes, if the VM is initialized and if loader!=NULL

  TODO: Make sure that there's no memory leaks
  is it possible to avoid hardcode of "avoid transform of instrument classes"
*/
void JNICALL
ClassFileLoadHook(jvmtiEnv *jvmti_env,
		  JNIEnv* jni,
		  jclass class_being_redefined,
		  jobject loader,
		  const char* name,
		  jobject protection_domain,
		  jint class_data_len,
		  const unsigned char* class_data,
		  jint* new_class_data_len,
		  unsigned char** new_class_data) 
{
  // Avoid transform of system classes
  enter_critical_section(); {
    if(!g_init || !loader) {
      return;
    }
  }  exit_critical_section();

  // Avoid transform of instrument classes
  const char *result = strstr(name,"org/objectweb/asm/");
  if(result
     || strcmp(name,"AddMethodEnterAdapter") == 0
     || strcmp(name,"NativeInterface") == 0
     || strcmp(name,"AddMethodAdapter") == 0
     || strcmp(name,"Instrument") == 0) {
    return;
  }

  enter_critical_section(); {

    jbyteArray barr = jni->NewByteArray(class_data_len);
    jni->SetByteArrayRegion(barr,0,class_data_len,(jbyte*)(class_data));
    jbyteArray new_barr = NULL; 
    new_barr = (jbyteArray) jni->CallStaticObjectMethod(g_cls,g_mid,barr);

    if (new_barr != NULL) {

      jint len = jni->GetArrayLength(new_barr);

      //printf("%s new len: %d\n",name,len);
      unsigned char *newclass;
      jvmtiError err = jvmti_env->Allocate(len,&newclass);

      if (err != JVMTI_ERROR_NONE) {
	printf("couldn't allocate space in class file load hook\n");
	return;
      }

      jni->GetByteArrayRegion(new_barr,0,len,(jbyte*)(newclass));

      *new_class_data_len = len;
      *new_class_data = newclass;
    }
    // cout << "Instrumented:";
    // printf("%s",name);
    // cout << endl;
  }exit_critical_section();
}


/* 
   The VM initialization event signals the completion of 
   VM initialization.

   SIDE EFFECTS:
   * Get the Instrument class and stores it in g_class

   * Gets the transform method from the "Instrument.class"
   and stores it in g_mid.

   * Sets init to 1 to indicate the VM init

   Return values:
   JNI_OK -> JVM will continue
   JNI_ERR -> JVM will disrupt initialization

   TODO:
   CHECK FOR ERRORS
*/
void JNICALL
VMInit(jvmtiEnv *jvmti_env,
       JNIEnv* jni,
       jthread thread) 
{
  jclass cls = jni->FindClass("Instrument");
  g_mid = jni->GetStaticMethodID(cls, "transform", "([B)[B");  
  g_cls = (jclass) jni->NewGlobalRef(cls);
  jni->DeleteLocalRef(cls);

  enter_critical_section(); {
    g_init = true;
  } exit_critical_section();
}


void parse_options(char *options) {
  if (options != NULL) {
    // printf("options str = %s\n",options);
    char *test_test = strstr(options,"test");
    if (test_test) {
      test = true;
      testnr = atoi((const char *)&(test_test[5]));
      //printf("options str = %s %d\n",test_test,testnr);
    }
  }
}


/* 
   This method is invoked by the JVM early in it's
   initialization. No classes have been loaded and no
   objects created.
   
   Tries to set capabilities and callbacks for the agent 
   If something goes wrong it will cause the JVM to 
   disrupt the initialization.

   Return values:
   JNI_OK -> JVM will continue
   JNI_ERR -> JVM will disrupt initialization

   TODO:
   Move setcapabilities etc to another function to increase readability
*/
JNIEXPORT jint JNICALL 
Agent_OnLoad(JavaVM *vm,
	     char *options,
	     void *reserved)
{
  jvmtiError error;
  jint res;
  jvmtiEnv *jvmti;

  parse_options(options);

  // Get jvmti env
  res = vm->GetEnv((void **)&jvmti, JVMTI_VERSION);
  if(res != JNI_OK){
    printf("ERROR GETTING JVMTI");
    return JNI_ERR;
  }

  // Set capabilities
  jvmtiCapabilities capabilities;
  memset(&capabilities,0,sizeof(jvmtiCapabilities));
  capabilities.can_generate_all_class_hook_events = 1;
  capabilities.can_tag_objects = 1;
  capabilities.can_access_local_variables = 1;
  capabilities.can_generate_object_free_events = 1;
  error = jvmti->AddCapabilities(&capabilities);
  if(error != JVMTI_ERROR_NONE) {
    printf("ERROR set capabilities");
    return JNI_ERR;
  }

  // Set callbacks
  jvmtiEventCallbacks callbacks;
  memset(&callbacks,0, sizeof(callbacks));
  callbacks.VMInit = &VMInit;
  callbacks.ClassFileLoadHook = &ClassFileLoadHook;
  callbacks.ObjectFree = &cbObjectFree;
  error = jvmti->SetEventCallbacks(&callbacks,sizeof(callbacks));
  if(error != JVMTI_ERROR_NONE) {
    printf("ERROR set Callbacks");
    return JNI_ERR;
  }

  // Enable EventNotification mode
  error = jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_VM_INIT, NULL);
  if(error != JVMTI_ERROR_NONE) {
    printf("ERROR setNotificationMode INIT");
    return JNI_ERR;
  }
  error = jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_CLASS_FILE_LOAD_HOOK, NULL);
  if(error != JVMTI_ERROR_NONE) {
    printf("ERROR setNotificationMode LOAD HOOK");
    return JNI_ERR;
  }
  error = jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_OBJECT_FREE, NULL);
  if(error != JVMTI_ERROR_NONE) {
    printf("ERROR setNotificationMode OBJECT FREE");
    return JNI_ERR;
  }
  g_jvmti = jvmti;

  error = jvmti->CreateRawMonitor((char *)"Callbacks Lock", &g_lock);
  if(error != JVMTI_ERROR_NONE) {
    printf("ERROR createrawmonitor");
    return JNI_ERR;
  }
  // jvmti->AddToBootstrapClassLoaderSearch(segment);
  jvmti->AddToSystemClassLoaderSearch(segment);

  return JNI_OK;
}


/* 
   This function is invoked by the JVM just before it unloads
   
   exports the events. If test is set it will instead call
   some test method.
*/
JNIEXPORT void JNICALL 
Agent_OnUnload(JavaVM *vm) {
  //printf("number of events %lu \n",eventlist.size());
  if (test) {
    test_fun(testnr,eventlist);
    return;
  }
  if (eventlist.size() > 0) {
    if (export_method == 0) {
      cout  << endl << "printing eventlist" << endl << endl;
    }
    else if (export_method == 1) {
      cout  << endl <<"exporting eventlist to file: " << filename << endl << endl;
    }
    printList(eventlist,export_method,filename);
  }
}
