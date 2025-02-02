#include "jni.h"
#include <string.h>
#include <stdlib.h>

jintArray JNIEnv::NewIntArray(size_t size)
{
   return(new int[size]);
}

void JNIEnv::SetIntArrayRegion(jintArray array, int start, int end, int *data)
{
   for (int i = start; i < end; i++)
   {
      array[i] = data[i];
   }
}

jfloatArray JNIEnv::NewFloatArray(size_t size)
{
   return(new float[size]);
}

void JNIEnv::SetFloatArrayRegion(jfloatArray array, int start, int end, float *data)
{
   for (int i = start; i < end; i++)
   {
      array[i] = data[i];
   }
}

jobjectArray JNIEnv::NewObjectArray(size_t size, jclass type, void *data)
{
   (void)data;
   if (type != 0)
   {
      return(nullptr);
   }

   if (size == 0)
   {
      return(nullptr);  // Return NULL for zero-sized array
   }

   // Allocate memory for the array of jobject pointers
   jobject *array = (jobject *)malloc(size * sizeof(jobject));
   if (!array)
   {
      return(nullptr);  // Return NULL if memory allocation fails
   }

   // Initialize the array with the initial element (or NULL)
   for (size_t i = 0; i < size; ++i)
   {
      array[i] = NULL;   // Set each element to the initial value
   }

   // Cast the array to jobjectArray (assuming jobjectArray is a pointer to an array of jobject)
   return(reinterpret_cast <jobjectArray>(array));
}

void JNIEnv::SetObjectArrayElement(jobjectArray array, int index, void *element)
{
   array[index] = element;
}

jclass JNIEnv::FindClass(const char *name)
{
   if (strcmp("[I", name) == 0)
   {
      return(0);
   }
   else
   {
      return(-1);
   }
}
