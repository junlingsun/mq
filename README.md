# Simple Java Message Queue (MQ) Framework

A lightweight message queue framework implemented in Java.

This project demonstrates how to build a minimal MQ system from scratch, including:

- Producer / Consumer model
- Broker server
- Topic-based message storage
- Netty-based network communication
- Basic message persistence

The goal of this project is educational — to understand how message queue systems like Kafka or RocketMQ work internally.

---

# Project Structure

```
mq-master
│
├── mq-common          # Common models (Message, Request, Response, Constants)
├── mq-client          # Producer and Consumer implementation
├── mq-server          # Broker server implementation
├── mq-sample-producer # Example producer
└── mq-sample-consumer # Example consumer
```

---

# Architecture Overview

```
Producer
   │
   ▼
MQ Client  →  Netty  →  Broker Server
                               │
                               ▼
                         Message Storage (Topic-based)
                               │
                               ▼
                           Consumer Pull
```

---

# Core Concepts

## 1. Producer

The producer:

- Creates messages
- Sends messages to broker
- Specifies topic

Message sending flow:

1. Build `Message`
2. Wrap into `MqRequest`
3. Send via Netty client
4. Receive `MqResponse`

---

## 2. Broker (Server)

The broker is responsible for:

- Receiving messages
- Storing messages by topic
- Handling consumer pull requests
- Returning messages to consumers

Main components:

- Netty server bootstrap
- Request handler
- Topic-based message store

---

## 3. Consumer

The consumer:

- Subscribes to a topic
- Sends pull request to broker
- Receives messages
- Processes messages

The current design uses a **pull model**, meaning consumers actively fetch messages.

---

# Message Flow

## Produce Flow

1. Producer sends message to broker
2. Broker stores message in topic queue
3. Broker returns success response

## Consume Flow

1. Consumer sends pull request
2. Broker reads messages from topic storage
3. Broker returns message list
4. Consumer processes messages

---

# Key Modules

## mq-common

Shared components:

- `Message`
- `MqRequest`
- `MqResponse`
- Constants and protocol definitions

---

## mq-client

Contains:

- Producer implementation
- Consumer implementation
- Netty client logic

Responsibilities:

- Connect to broker
- Send produce/pull requests
- Handle responses

---

## mq-server

Contains:

- Netty server bootstrap
- Request handler
- Message storage logic

Responsibilities:

- Accept connections
- Parse requests
- Store and retrieve messages

---

# How to Run

## 1. Start MQ Server

Run the broker main class inside:

```
mq-server
```

This will:

- Start Netty server
- Listen for producer and consumer connections

---

## 2. Start Producer

Run:

```
mq-sample-producer
```

Producer will:

- Connect to broker
- Send messages to a topic

---

## 3. Start Consumer

Run:

```
mq-sample-consumer
```

Consumer will:

- Connect to broker
- Pull messages from topic
- Print messages

---

# Technology Stack

- Java 8+
- Netty
- Maven
- Concurrent collections for in-memory storage

---

# Design Highlights

- Clear separation between client and server
- Topic-based message storage
- Pull-based consumption model
- Simple request/response protocol
- Multi-module Maven structure

---

# Limitations

This is a simplified educational implementation.

Missing features compared to production MQ systems:

- No message replication
- No persistent disk storage (if in-memory)
- No partitioning
- No offset management
- No consumer group support
- No high availability
- No retry or dead-letter queue
- No back-pressure control

---

# Possible Improvements

- Add persistent storage (file-based or database)
- Add consumer group and offset tracking
- Add push-based consumption
- Add message acknowledgement
- Add replication mechanism
- Add partition support
- Add monitoring metrics
- Add graceful shutdown

---

# Learning Purpose

This project helps understand:

- How message queues work internally
- How producers and consumers communicate
- How a broker handles topic-based storage
- How pull-based consumption works
- How to design a simple messaging protocol
- How Netty handles asynchronous network communication

---

# Summary

This is a minimal but complete message queue implementation demonstrating:

- Producer → Broker → Consumer architecture
- Topic-based message storage
- Pull consumption model
- Netty-based networking
- Modular project design

It serves as a foundation for building more advanced distributed messaging systems.
