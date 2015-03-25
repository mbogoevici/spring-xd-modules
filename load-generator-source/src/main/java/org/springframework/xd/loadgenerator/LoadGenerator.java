/*
 * Copyright 2015 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.xd.loadgenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Is a source module that generates a series of fixed size messages to be
 * dispatched to a stream.  The load-generator is used to test the performance of
 * XD in different environments.
 *
 * @author Glenn Renfro
 */
public class LoadGenerator extends MessageProducerSupport {

	private int producers;

	private int messageSize;

	private int messageCount;

	private final AtomicBoolean running = new AtomicBoolean(false);

	private ExecutorService executorService;

	Logger logger = LoggerFactory.getLogger(LoadGenerator.class);

	public LoadGenerator(int producers, int messageSize, int messageCount) {
		this.producers = producers;
		this.messageSize = messageSize;
		this.messageCount = messageCount;
	}

	@Override
	protected void doStart() {
		executorService = Executors.newFixedThreadPool(producers);
		if (running.compareAndSet(false, true)) {
			for (int x = 0; x < producers; x++) {
				executorService.execute(new Producer(x));
			}
		}
	}

	@Override
	protected void doStop() {
		if (running.compareAndSet(true, false)) {
			executorService.shutdown();
		}
	}

	protected class Producer implements Runnable {

		int producerId;

		public Producer(int producerId) {
			this.producerId = producerId;
		}

		private void send() {
			logger.info("Producer " + producerId + " sending " + messageCount + " messages");
			for (int x = 0; x < messageCount; x++) {
				final byte[] message = createMessage(x);
				sendMessage(new TestMessage(message));
			}
			logger.info("All Messages Dispatched");
		}

		/**
		 * Creates a message that can be consumed by the Rabbit perfTest Client
		 *
		 * @param sequenceNumber a number to be prepended to the message
		 * @return a byte array containing a series of numbers that match the message size as
		 * specified by the messageSize constructor arg.
		 */
		private byte[] createMessage(int sequenceNumber) {
			byte message[] = new byte[messageSize];
			return message;
		}

		public void run() {
			send();
		}

		private class TestMessage implements Message<byte[]> {
			private final byte[] message;

			private final TestMessageHeaders headers;

			public TestMessage(byte[] message) {
				this.message = message;
				this.headers = new TestMessageHeaders(null);
			}

			@Override
			public byte[] getPayload() {
				return message;
			}

			@Override
			public MessageHeaders getHeaders() {
				return headers;
			}

			class TestMessageHeaders extends MessageHeaders {
				public TestMessageHeaders(Map<String, Object> headers) {
					super(headers, ID_VALUE_NONE, -1L);
				}
			}
		}
	}
}
