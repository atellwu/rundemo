# Swallow使用说明

[TOC]

# swallow基础概念

Swallow 是什么:
* Swallow是一个基于Topic的异步消息传送系统。Swallow使用发布/订阅消息的传送模型，消息发布者指定Topic并发送消息到Swallow消息服务器，消息订阅者则指定Topic并从Swallow消息服务器订阅消息。
* Swallow的发布/订阅模型。消息由Producer发布，ProducerServer负责接收并存储消息到DB。ConsumerServer负责从DB获取消息，并推送给Consumer。
* Swallow支持集群订阅者。在集群中，使用相同ConsumerId(例如Consumer A)的Consumer，将会视作同一个Consumer（同一个Consumer消费的Message将不会重复）。例如，假设一个有2台机器(主机1和主机2)的集群，ConsumerId都是“Consumer-A”，那么同一则Message，将要么被“主机1”获取，要么被“主机2”获取，不会被两者均获取。

# swallow系统接入流程
## 申请topic
# swallow使用说明
## 使用swallow发送消息

     public class SyncProducerExample {
         public static void main(String[] args) throws Exception {
             ProducerConfig config = new ProducerConfig();  //(1)
	     config.setMode(ProducerMode.SYNC_MODE);  (2)
	     Producer p = ProducerFactoryImpl.getInstance().createProducer(Destination.topic("example"), config);  (3)
	     for (int i = 0; i < 10; i++) {
	         String msg = "消息-" + i;
	         p.sendMessage(msg);  (4)
	         System.out.println("Sended msg:" + msg);
	         Thread.sleep(500);
	     }
	 }
     }

1.使用swallow发送消息时，首先需要对发送端进行配置，这由ProducerConfig完成。由于ProducerConfig没有提供构造函数，所以只能调用默认构造函数，这样所有属性都会被设置为默认值。
2.如果想更改默认设置，则可以调用相应的setter函数进行设置，下图列出了所有可配置属性及其setter函数。

    ![图片君匆匆加载中。。。](https://github.com/lmdyyh/rundemo/raw/master/picture/producer.png "配置属性及其setter函数")

3.设置好发送端属性后就可以对生产者对象进行构造。ProducerFactoryImpl实现了ProducerFactory，并且其自身为单例对象，调用静态方法getInstance()返回这个单例工厂对象，执行createProducer会返回ProducerImpl实例，而ProducerImpl自身实现了接口Producer。作为生产者，需要绑定消息发送的目的地，Destination实现了对目的地的抽象，其静态方法topic(String name)会返回主题是name的消息目的地。
4.Producer唯一定义了发送消息的方法sendMessage,下图列出了不同版本的sendMessage。

    ![图片君匆匆加载中。。。](https://github.com/lmdyyh/rundemo/raw/master/picture/producer_attr.png "函数sendMessage")


## 使用swallow接收消息
# swallow常见问题以及处理
##
