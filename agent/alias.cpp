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
int export_method = 1;
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

/******************************************************************************/
/* javastr->cstring | gettag                                                  */
/******************************************************************************/

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

   */
JNIEXPORT void JNICALL Java_NativeInterface_storeField
(JNIEnv *env,
 jclass native_interface,
 jobject callee,
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
      jlong caller_tag = get_tag(caller);
      jlong callee_tag = get_tag(callee);
      jlong value_tag = get_tag(value);
      jlong old_value_tag = get_tag(old_value);

      string cp_field = toCPS(env,name);
      string cp_desc = toCPS(env,desc);

      string cp_callee;
      if (callee_tag == 0) {
	cp_callee = toCPS(env,owner);
      }

      string cp_caller;
      if (caller_tag == 0) {
	cp_caller = toCPS(env,static_caller);
      }
      StoreField *event = new StoreField(cp_field,
					 cp_desc,
					 cp_caller,
					 cp_callee,
					 caller_tag,
					 callee_tag,
					 value_tag,
					 old_value_tag);
      eventlist.push_back(event);

    } exit_critical_section(); 
  }
}

/* 
   TODO:

   */
JNIEXPORT void JNICALL Java_NativeInterface_loadField
(JNIEnv *env,
 jclass native_interface,
 jobject callee,
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
      jlong caller_tag = get_tag(caller);
      jlong callee_tag = get_tag(callee);
      jlong value_tag = get_tag(value);

      string cp_field = toCPS(env,name);
      string cp_desc = toCPS(env,desc);

      string cp_callee;
      if (callee_tag == 0) {
	cp_callee = toCPS(env,owner);
      }

      string cp_caller;
      if (caller_tag == 0) {
	cp_caller = toCPS(env,static_caller);
      }

      GetField *event = new GetField(cp_field,
				     cp_desc,
				     cp_caller,
				     cp_callee,
				     caller_tag,
				     callee_tag,
				     value_tag);
      eventlist.push_back(event);
    }exit_critical_section(); 
  }
}

/*
   TODO:
     * Move get caller part to a separate function
     * Move get arg objs to a separate function
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
    string cp_name = toCPS(env,met);
    string cp_desc = toCPS(env,desc);
    jlong callee_tag = get_tag(callee);
    string cp_callee;
    if (callee_tag == 0) {
      cp_callee = toCPS(env,staticcallee);
    }
    if (callee_tag == 0 && cp_callee.compare("")==0) {
      callee_tag = g_objectid++;
      g_jvmti->SetTag(callee,callee_tag);
    }
    jlong caller_tag = 0;
    string cp_caller;
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
  	//strcat(source_name,methodName2);
	cp_caller = source_name;
	cp_caller.append(methodName2);
  	//cp_caller = source_name;
	if (source_name) {
	  g_jvmti->Deallocate((unsigned char *)source_name);
	}
      }
      else {
  	jobject callerobj = NULL;
  	jvmtiError error = g_jvmti->GetLocalObject(thread,2,0,&callerobj);
  	if (callerobj != NULL) {
  	  error = g_jvmti->GetTag(callerobj,&caller_tag);
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
  	    Allocation *event = new Allocation ("N/A",argtags[i],caller_tag,"-");
  	    eventlist.push_back(event);
  	  }
  	}
      }
    }

    if (methodEnter){
      MethodCall *event = new MethodCall (cp_name,
  					  cp_desc,
  					  cp_callee,
  					  cp_caller,
  					  callee_tag,
  					  caller_tag,
  					  argtags,
  					  argcount);
      eventlist.push_back(event);
    }
  }exit_critical_section();
}


/* 
   TODO:
     * Move get caller part to a separate function
     * Move get out of scope objs to a separate function
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
      jlong returned_tag = get_tag(returned);
      jlong callee_tag = get_tag(callee);
      string cp_method = toCPS(env,met);
      string cp_desc = toCPS(env,desc);
      string cp_staticcallee;
      if (callee_tag == 0) {
	cp_staticcallee = toCPS(env,staticcallee);
      }
      
      string cp_staticcaller;
      jlong caller_tag = 0;

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
	  //strcat(source_name,methodName2);
	  cp_staticcaller = source_name;
	  cp_staticcaller.append(methodName2);
	  //cp_staticcallercaller = source_name;
	  if (source_name) {
	    g_jvmti->Deallocate((unsigned char *)source_name);
	  }
	}
	else {
	  jobject callerobj = NULL;
	  jvmtiError error = g_jvmti->GetLocalObject(thread,2,0,&callerobj);
	  if (callerobj != NULL) {
	    error = g_jvmti->GetTag(callerobj,&caller_tag);
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
      Returned *event = new Returned(cp_method,
				     cp_desc,
				     cp_staticcallee,
				     cp_staticcaller,
				     caller_tag,
				     callee_tag,
				     returned_tag,
				     outobjs,
				     objcounter);
      eventlist.push_back(event);
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
    // if (stored_tag != 0) {
    //   return;
    // }
    // else 
      if (stored_tag == 0 && stored != NULL) {
	g_jvmti->SetTag(stored,g_objectid++);
	stored_tag = g_objectid - 1;
      }
    
    if (alloc) {
      string type = toCPS(env,desc);
      jlong caller_tag = get_tag(caller);
      string cp_staticcaller;
      if (caller_tag == 0) {
	cp_staticcaller = toCPS(env,staticcaller);
      }
      if (caller_tag == 0 && cp_staticcaller.compare("") == 0) {
	jvmtiFrameInfo *frame;
	g_jvmti->Allocate(3*sizeof(jvmtiFrameInfo),(unsigned char**)&frame);

	jint count;
	g_jvmti->GetStackTrace(thread,1,2,frame,&count);
	if (count > 1) {
	  char *methodName2 = NULL;
	  g_jvmti->GetMethodName(frame[0].method, &methodName2,NULL, NULL);


	  jint access_flags = 0;
	  g_jvmti->GetMethodModifiers(frame[0].method,&access_flags);
	  //printf("calling met: %s access flags: %d \n",methodName2, access_flags);
	  // flags == 9
	  
	  if (access_flags >= 8) {
	    jclass declaring_class;
	    char *source_name;
	    jvmtiError error = g_jvmti->GetMethodDeclaringClass(frame[0].method,&declaring_class);
	    if (error != JVMTI_ERROR_NONE) {
	      printf("errrrrrrror");
	    }
	    error = g_jvmti->GetClassSignature(declaring_class,&source_name,NULL);
	    if (error != JVMTI_ERROR_NONE) {
	      printf("errrrrrrror");
	    }
	    //strcat(source_name,methodName2);
	    cp_staticcaller = source_name;
	    cp_staticcaller.append(methodName2);
	    //cp_staticcallercaller = source_name;
	    if (source_name) {
	      g_jvmti->Deallocate((unsigned char *)source_name);
	    }
	  }
	  else {
	    jobject callerobj = NULL;
	    jvmtiError error = g_jvmti->GetLocalObject(thread,1,0,&callerobj);
	    if (callerobj != NULL) {
	      error = g_jvmti->GetTag(callerobj,&caller_tag);
	      if (error != JVMTI_ERROR_NONE) {
		printf("errrrrrrror %d \n",error);
	      }
	      if (caller_tag == 0) {
		caller_tag = g_objectid++;
		g_jvmti->SetTag(callerobj,caller_tag);
	      }
	    }
	  }
	}	
      }
      //cout << to_string(caller_tag) << " " << cp_staticcaller << endl;

      Allocation *event = new Allocation (type,stored_tag,caller_tag,cp_staticcaller);
      eventlist.push_back(event);
    }
  }exit_critical_section();
}


/* 
   Catches event when the gc frees a tagged object
*/
void JNICALL
cbObjectFree(jvmtiEnv *jvmti_env,
	     jlong tag) 
{
  Deallocation *event = new Deallocation (tag);
  eventlist.push_back(event);
}


/******************************************************************************/
/* System Callbacks                                                           */
/******************************************************************************/

// void JNICALL
// Exception(jvmtiEnv *jvmti_env,
//             JNIEnv* jni_env,
//             jthread thread,
//             jmethodID method,
//             jlocation location,
//             jobject exception,
//             jmethodID catch_method,
// 	  jlocation catch_location) {
//   printf("exception\n");
// }



// void JNICALL
// ExceptionCatch(jvmtiEnv *jvmti_env,
//             JNIEnv* jni_env,
//             jthread thread,
//             jmethodID method,
//             jlocation location,
// 	       jobject exception) {
//   printf("exception catch\n");
// }














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


    if(!g_init || !loader) {
      return;
    }

   

    // Avoid transformation of instrument classes
    const char *result = strstr(name,"org/objectweb/asm/");
    if(result
       || strcmp(name,"AddMethodEnterAdapter") == 0
       || strcmp(name,"NativeInterface") == 0
       || strcmp(name,"AddMethodAdapter") == 0
       || strcmp(name,"Instrument") == 0) {
      return;
    }
    
//     JNIEnv* env = jni;
// jclass cls = env->GetObjectClass(loader);

// // First get the class object
// jmethodID mid = env->GetMethodID(cls, "getClass", "()Ljava/lang/Class;");
// jobject clsObj = env->CallObjectMethod(loader, mid);

// // Now get the class object's class descriptor
// cls = env->GetObjectClass(clsObj);

// // Find the getName() method on the class object
// mid = env->GetMethodID(cls, "getName", "()Ljava/lang/String;");

// // Call the getName() to get a jstring object back
// jstring strObj = (jstring)env->CallObjectMethod(clsObj, mid);

// // Now get the c string from the java jstring object
// const char* str = env->GetStringUTFChars(strObj, NULL);

// // Print the class name
//  printf("Classloader %s is loading: %s \n\n", str,name);

//  if (strcmp(str,"sun.misc.Launcher$AppClassLoader") != 0) {
//    return;
//  }

// // Release the memory pinned char array
// env->ReleaseStringUTFChars(strObj, str);








  enter_critical_section(); {




    //printf("c++ instrumenting: %s \n",name);

    // printf("%s %d \n",name,class_data_len);
  
    jbyteArray barr = jni->NewByteArray(class_data_len);

    jni->SetByteArrayRegion(barr,0,class_data_len,(jbyte*)(class_data));


    jbyteArray new_barr = (jbyteArray) jni->CallStaticObjectMethod(g_cls,g_mid,barr);

    if (new_barr != NULL) {

      jint len = jni->GetArrayLength(new_barr);

      // printf("%s new len: %d\n",name,len);
      unsigned char *newclass;
      jvmtiError err = jvmti_env->Allocate(len,&newclass);

      if (err != JVMTI_ERROR_NONE) {
	printf("couldn't allocate space in class file load hook\n");
	return;
      }

      jni->GetByteArrayRegion(new_barr,0,len,(jbyte*)(newclass));

      *new_class_data_len = len;
      *new_class_data = newclass;
      //printf("c++ done instrumenting: %s \n\n",name);
    }
    else {
      //printf("c++ failed to instrument: %s \n\n",name);
    }

  }exit_critical_section();
  // printf("done\n\n");
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


void parse_options(char *options) 
{
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
  capabilities.can_generate_exception_events = 1;
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
  // callbacks.Exception = &Exception;
  // callbacks.ExceptionCatch = &ExceptionCatch;
  error = jvmti->SetEventCallbacks(&callbacks,sizeof(callbacks));
  if(error != JVMTI_ERROR_NONE) {
    printf("ERROR set Callbacks");
    return JNI_ERR;
  }

  // Enable EventNotification mode
  // jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_EXCEPTION, NULL);
  // jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_EXCEPTION_CATCH, NULL);


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
  // jvmti->AddToSystemClassLoaderSearch("/home/erik/Desktop/dacapo-9.12-bach.jar");
  // jvmti->AddToSystemClassLoaderSearch("/home/erik/java-alias-agent/agent/scratch/jar");

  // jvmti->AddToBootstrapClassLoaderSearch("/home/erik/java-alias-agent/agent/scratch/jar");

  return JNI_OK;
}


/* 
   This function is invoked by the JVM just before it unloads
   
   exports the events. If test is set it will instead call
   some test method.
*/
JNIEXPORT void JNICALL 
Agent_OnUnload(JavaVM *vm)
{
  //printf("number of events %lu \n",eventlist.size());
  if (test) {
    test_fun(testnr,eventlist);
    return;
  }
  if (eventlist.size() > 0) {
    if (export_method == 0) {
      cout  << endl << "printing " << to_string(eventlist.size()) << " events" << endl << endl;
    }
    else if (export_method == 1) {
      cout  << endl <<"exporting " << to_string(eventlist.size()) << " events to file: " << filename << endl << endl;
    }
    printList(eventlist,export_method,filename);
  }
}
