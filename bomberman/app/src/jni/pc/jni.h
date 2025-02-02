#ifndef JNI_H
#define JNI_H

#define JNIEXPORT
#define JNICALL

typedef int                  jint;
typedef float                jfloat;
typedef char                 jbyte;
typedef long                 jlong;
typedef int                  jclass;
typedef void *               jobject;
typedef jint *               jintArray;
typedef jfloat *             jfloatArray;
typedef jobject *            jobjectArray;
typedef unsigned long long   size_t;
class JNIEnv
{
public:
   jintArray NewIntArray(size_t size);
   void SetIntArrayRegion(jintArray array, int start, int end, int *data);
   jfloatArray NewFloatArray(size_t size);
   void SetFloatArrayRegion(jfloatArray array, int start, int end, float *data);
   jobjectArray NewObjectArray(size_t size, jclass type, void *data);
   void SetObjectArrayElement(jobjectArray array, int index, void *element);
   jclass FindClass(const char *name);
};

#endif
