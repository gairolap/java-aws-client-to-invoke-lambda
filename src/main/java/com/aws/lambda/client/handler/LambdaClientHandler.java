/**
 * AWS lambda client handler class for invoking the given lambda function. 
 */
package com.aws.lambda.client.handler;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaAsyncClient;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LambdaClientHandler implements RequestHandler<Map<String, String>, Object> {

	public Object handleRequest(Map<String, String> request, Context context) {

		return this.invokeAWSLambdaFunc(request);
	}

	/**
	 * Invoke the given lambda function.
	 * 
	 * @param {@linkplain Map<String, String>}.
	 * @return {@linkplain Object}.
	 */
	public Object invokeAWSLambdaFunc(Map<String, String> request) {

		ObjectMapper mapper = new ObjectMapper();
		AWSLambda lambdaClient = AWSLambdaAsyncClient.builder().withRegion(request.get("region")).build();

		InvokeRequest invokeRequest = new InvokeRequest();
		String lambdaFuncNm = request.get("lambdaToInvoke");

		if (StringUtils.isNullOrEmpty(lambdaFuncNm)) {
			return "Lambda function name cannot be empty!";
		}
		try {
			invokeRequest.setFunctionName(request.get("lambdaToInvoke"));
			request.remove("lambdaToInvoke");
			invokeRequest.setPayload(mapper.writeValueAsString(request));
			InvokeResult response = lambdaClient.invoke(invokeRequest);
			return byteBufferToString(lambdaClient.invoke(invokeRequest).getPayload(), StandardCharsets.UTF_8);
		} catch (JsonProcessingException jsonPrsngExcp) {
			jsonPrsngExcp.printStackTrace();
			return "Error occurred while invoking processing request params!";
		}
	}

	/**
	 * Convert the response from ByteBuffer to String.
	 * 
	 * @param {@linkplain ByteBuffer}.
	 * @param {@linkplain Charset}.
	 * @return {@linkplain String}.
	 */
	private static String byteBufferToString(ByteBuffer buffer, Charset charset) {

		byte[] bytes;
		if (buffer.hasArray()) {
			bytes = buffer.array();
		} else {
			bytes = new byte[buffer.remaining()];
			buffer.get(bytes);
		}
		return new String(bytes, charset);
	}

}