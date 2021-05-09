#include <com_score_rahasak_utils_OpusEncoder.h>
#include <string.h>
#include <android/log.h>
#include <opus/include/opus.h>
#include <stdio.h>
#include <malloc.h>

char encodingLogMsg[255];
OpusEncoder *enc;

opus_int32 ENCODING_SAMPLING_RATE;
int ENCODING_CHANNELS;
int APPLICATION_TYPE = OPUS_APPLICATION_VOIP;
int ENCODING_FRAME_SIZE;
const int MAX_PAYLOAD_BYTES = 1500;


JNIEXPORT jboolean JNICALL Java_com_example_opustest_utils_OpusEncoder_nativeInitEncoder (JNIEnv *env, jobject obj, jint samplingRate, jint numberOfChannels, jint frameSize)
{
	ENCODING_FRAME_SIZE = frameSize;
	ENCODING_SAMPLING_RATE = samplingRate;
	ENCODING_CHANNELS = numberOfChannels;

	int error;
	int size;

	size = opus_encoder_get_size(1);
	enc = malloc(size);
	error = opus_encoder_init(enc, ENCODING_SAMPLING_RATE, ENCODING_CHANNELS, APPLICATION_TYPE);

	sprintf(encodingLogMsg, "Initialized Encoder with ErrorCode: %d", error);
	__android_log_write(ANDROID_LOG_DEBUG, "Native Code:", encodingLogMsg);

	return error;
}

JNIEXPORT jint JNICALL Java_com_example_opustest_utils_OpusEncoder_nativeEncodeBytes (JNIEnv *env, jobject obj, jshortArray in, jbyteArray out)
{
	jint inputArraySize = (*env)->GetArrayLength(env, in);
	jint outputArraySize = (*env)->GetArrayLength(env, out);

	jshort* audioSignal = (*env)->GetShortArrayElements(env, in, 0);

	unsigned char *data = (unsigned char*)calloc(MAX_PAYLOAD_BYTES,sizeof(unsigned char));
	int dataArraySize = opus_encode(enc, audioSignal, ENCODING_FRAME_SIZE, data, MAX_PAYLOAD_BYTES);

	if (dataArraySize >=0)
	{
		if (dataArraySize <= outputArraySize)
		{
			(*env)->SetByteArrayRegion(env,out,0,dataArraySize,data);
		}
		else
		{
			sprintf(encodingLogMsg, "Output array of size: %d to small for storing encoded data.", outputArraySize);
			__android_log_write(ANDROID_LOG_DEBUG, "Native Code:", encodingLogMsg);

			return -1;
		}
	}

	(*env)->ReleaseShortArrayElements(env,in,audioSignal,JNI_ABORT);

	return dataArraySize;
}

JNIEXPORT jboolean JNICALL Java_com_example_opustest_utils_OpusEncoder_nativeReleaseEncoder (JNIEnv *env, jobject obj)
{
    free(enc);

    return 1;
}



