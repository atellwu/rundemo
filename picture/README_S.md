# Swallow使用说明

* * * 

### 1. Swallow基础概念

* Swallow 是什么:
	* Swallow是一个`基于Topic的异步消息传送系统`。Swallow使用`发布/订阅消息`的传送模型，`消息发布者`指定Topic并发送消息到Swallow消息服务器,`消息订阅者`则指定Topic并从Swallow消息服务器订阅消息。
	* Swallow的发布/订阅模型。消息由Producer发布，ProducerServer负责接收并存储消息到DB。ConsumerServer负责从DB获取消息，并推送给Consumer。
	* Swallow`支持集群订阅者`。在集群中，使用相同ConsumerId(例如Consumer A)的Consumer，将会视作同一个Consumer（同一个Consumer消费的Message将不会重复）。例如，假设一个有2台机器(主机1和主机2)的集群，ConsumerId都是“Consumer-A”，那么`同一则Message，将要么被“主机1”获取，要么被“主机2”获取，不会被两者均获取`。

### 2. swallow名词解释

* Producer表示生产消息的主体，将消息发送到目的地Destination。
* Consumer表示消费消息的主体，从Destination中获取消息。
* Destination表示消息的目的地，也就是消息在swallow中驻留的地方。swallow中定义了两种目的地topic和queue，目前只实现了topic。
* 同步模式表示消息发送成功或者超时才返回。
* 异步模式表示不管消息是否发送成功都立即返回。
* 消息持久化表示消息会持久化到磁盘或者文件，server重启后消息不会丢失。非持久化与之相反，server重启后消息会丢失。

### 3. Swallow可用系统

### 4. Swallow系统接入流程

* 申请topic

### 4. Swallow使用说明

* 使用swallow发送消息

	<pre><code>
	public class SyncProducerExample{
		public static void main(String[] args) throws Exception {
 	       		producerConfig config = new ProducerConfig();  //(1)
	       		config.setMode(ProducerMode.SYNC_MODE);  //(2)
	       		Producer p = ProducerFactoryImpl.getInstance().createProducer(Destination.topic("example"), config);  //(3)
	       		for (int i = 0; i < 10; i++) {
	            	         		String msg = "消息-" + i;
		         		p.sendMessage(msg);  //(4)
		         		System.out.println("Sended msg:" + msg);
		         		Thread.sleep(500);
	                	}
                      	}
                }
                </code></pre>

 
 1.使用swallow发送消息时，首先需要对发送端进行配置，这由ProducerConfig完成。由于ProducerConfig没有提供构造函数，所以只能调用默认构造函数，这样所有属性都会被设置为默认值。下图列出了生产者的所有属性及其默认值。

 ![图片君匆匆加载中。。。](http://code.dianpingoa.com/arch/swallow/raw/master/readme/8.png "Producer属性")

 	* mode表示producer表示工作模式。
 	* asyncRetryTimes表示异步模式下发送失败重试次数。
 	* syncRetryTimes表示同步模式下发送失败重试次数。
 	* zipped表示是否对待发送消息进行压缩。
 	* threadPoolSize表示异步模式时，线程池大小。
 	* sendMsgLeftLastSession表示异步模式时，是否重启续传。

 2.如果想更改默认设置，则可以调用相应的setter函数进行设置，下图列出了所有可配置属性及其getter和setter函数。生产者共有3中模式，即同步模式ProducerMode.SYNC_MODE,异步模式ProducerMode.ASYNC_MODE和ProducerMode.ASYNC_SEPARATELY_MODE。
     
 ![图片君匆匆加载中。。。](http://code.dianpingoa.com/arch/swallow/raw/master/readme/9.png "函数sendMessage")
     
 3.设置好发送端属性后就可以对生产者对象进行构造。ProducerFactoryImpl实现了ProducerFactory，并且其自身为单例对象，调用静态方法getInstance()返回这个单例工厂对象，执行createProducer会返回ProducerImpl实例，而ProducerImpl自身实现了接口Producer。作为生产者，需要绑定消息发送的目的地，Destination实现了对目的地的抽象，其静态方法topic(String name)会返回主题是name的消息目的地。
     
 4.Producer唯一定义了发送消息的方法sendMessage,下图列出了不同版本的sendMessage。对于需要发送的消息，如果是String类型，则直接发送；如果是其他类型则会被序列化为json字符串进行传输。开发时需要注意：
     
 	a.请确保content对象的类型具有默认构造方法。
 	b.尽量保证content对象是简单的类型(如String/基本类型包装类/POJO)。如果content是复杂的类型，建议在您的项目上线之前，在接收消息端做测试，验证是否能够将content正常反序列化。
      
![图片君匆匆加载中。。。](http://code.dianpingoa.com/arch/swallow/raw/master/readme/10.png "配置属性及其setter函数")


* 使用swallow接收消息

	<pre><code>
	public class DurableConsumerExample {
	   	public static void main(String[] args) {
	      		ConsumerConfig config = new ConsumerConfig();  //(1)
	      		//以下根据自己情况而定，默认是不需要配的
	      		config.setThreadPoolSize(1);  //(2)
	      		Consumer c = ConsumerFactoryImpl.getInstance().createConsumer(Destination.topic("example"), "myId", config);  //(3)
	      		c.setListener(new MessageListener() {  //(4)
	         			@Override
	         			public void onMessage(Message msg) {
	            				System.out.println(msg.getContent());
	        			 }
	      		});
	      		c.start();  //(5)
	   	}
	}
	</code></pre>


1.使用swallow接收消息时，首先需要对接收端进行配置，这由ConsumerConfig完成。由于ConsumerConfig没有提供构造函数，所以只能调用默认构造函数，这样所有属性都会被设置为默认值。下图列出了消费者的所有属性及其默认值。
     
![图片君匆匆加载中。。。](http://code.dianpingoa.com/arch/swallow/raw/master/readme/11.png "Consumer属性")
     
* threadPoolSize表示consumer处理消息的线程池线程数，默认为1。注意，如果设置成多线程，那么会有多线程同时接收消息，这样的话接收的消息就无法保证其先后顺序。
* messageFilter表示consumer只消费“Message.type属性包含在指定集合中”的消息。
* consumerType表示consumer的类型，包括2种类型：
 
 	a. AT_LEAST：尽量保证消息最少消费一次，不出现消息丢失的情况。(注意：只是尽量保证，而非绝对保证。)
	b. NON_DURABLE：临时的消费类型，从当前的消息开始消费，不会对消费状态进行持久化，Server重启后将重新开始。
          
* delayBaseOnBackoutMessageException表示当MessageListener.onMessage(Message)抛出BackoutMessageException异常时，2次重试之间最小的停顿时间。
* delayUpperboundOnBackoutMessageException表示当MessageListener.onMessage(Message)抛出BackoutMessageException异常时，2次重试之间最大的停顿时间。
* retryCountOnBackoutMessageException表示当MessageListener.onMessage(Message)抛出BackoutMessageException异常时，最多重试的次数。
* startMessageId表示当需要在建立连接的时候指定读取消息的位置，可以设置该参数指定 。
      
2.如果想更改默认设置，则可以调用相应的setter函数进行设置，下图列出了所有可配置属性及其getter和setter函数。

![图片君匆匆加载中。。。](http://code.dianpingoa.com/arch/swallow/raw/master/readme/12.png "消费者配置函数")
     
3.设置好接收端属性后就可以对消费者对象进行构造。ConsumerFactoryImpl实现了ConsumerFactory，并且其自身为单例对象，调用静态方法getInstance()返回这个单例工厂对象，执行createConsumer会返回ConsumerImpl实例，而ConsumerImpl自身实现了接口Consumer。作为消费者，需要绑定消息发送的目的地，Destination实现了对目的地的抽象，其静态方法topic(String name)会返回主题是name的消息目的地，该名字需要与所感兴趣的生产者指定的目的地名称一致。
     
4.Consumer唯一定义了异步接受消息的方法setListener,该方法需要一个实现MessageListener接口的实例作为参数。MessageListener接口唯一定义了onMessage(Message msg)方法，客户端只需要实现该方法，将消息处理逻辑写入其中即可。

5.调用start()方法启动客户端程序。程序内部会使用Netty框架实现网络通信过程。
     
* * * 

### 5. Swallow常见问题以及处理

* #### a. 如何查看我的消费是否有延迟、延迟多少条消息？
	* 从[CAT](http://cat.dp/)中查看`Swallow`项目的`Transaction`，可以获得相应的信息（[传送门](http://cat.dp/cat/r/t?op=view&domain=Swallow)）。
	* 以dp\_action这个topic为例（`仅作示例，具体到自己的topic，请做相应变通`），先找到`In:dp_action`这个type：
	![Swallow Transaction In CAT](http://code.dianpingoa.com/arch/swallow/raw/master/readme/1.png)
	* 上图右边对应的是当前该topic的producer生产的`消息总量`，点击`In:dp_action`链接，可以看到每个producer产生的消息数量：
	![Producer Count In CAT](http://code.dianpingoa.com/arch/swallow/raw/master/readme/2.png)
	* 返回上一级，在同一级页面中，找到`Out:dp_action`这个type，对应从consumer server发出的消息数量：
	![Producer Count In CAT](http://code.dianpingoa.com/arch/swallow/raw/master/readme/3.png)
	* `Out:dp_action`对应的数量为消费这个topic的`所有consumer`消费的`消息总量`，点击进入，可以看到`每个消费者单台消费机`的消费数量：
	![Producer Count In CAT](http://code.dianpingoa.com/arch/swallow/raw/master/readme/4.png)
	* 对于一个consumer id来说，消费的消息总量，应该等于producer生产的消息总量（In:dp\_action的数量），`如果消费的消息总量小于生产的消息总量，那么消费是有延迟的`。

* #### b. 如何查看我的Consumer消费一条消息的平均时间？
	* 从[CAT](http://cat.dp/)中查看`Consumer ID对应项目`的Transaction，找到`MsgConsumed`和`MsgConsumeTried`这两个type：
	![Producer Count In CAT](http://code.dianpingoa.com/arch/swallow/raw/master/readme/5.png)
	* `MsgConsumed`表示`consumer server给这个consumer推送的消息数量`，`MsgConsumeTried`表示`consumer尝试消费消息的次数`，如果存在失败重试，则MsgConsumeTried数量可能会比MsgConsumed更多。
	* 右边的三列可以看出`consumer调用onMessage回调函数耗费的最小、最大以及平均时间`，如果consumer消费状况一直良好，突然某个时刻开始有消费延时，可以观察一下这里的平均时间是不是比之前更高，如果平均消费时间比正常情况高出很多，可能会造成消费延时。

* #### c. 我的Consumer有延时，该怎么确认问题所在？
	* 首先观察consumer的`平均消费时间`是否存在异常，如果consumer的平均消费时间`比正常情况高出许多`，说明onMessage回调函数依赖的服务存在问题，可以考虑_最近的代码变更_，或询问_依赖的服务_是否存在故障。
	* 如果consumer的`平均消费时间一直很高`，说明consumer的消费线程数太少，可以考虑`修改配置文件增加消费线程数`，或者`扩容应用增加消费机`。
	* 在cat中观察consumer的problem，`如果swallow相关异常过多，请及时联系swallow团队成员`。
	* 如果consumer的平均消费时间`一直正常、没有发生突变`，则有可能是swallow的consumer server负载较高或存在其他故障，`此时请及时联系swallow团队成员`。

* #### d. 我的Consumer堵了，该怎么确认问题所在？
	* 首先`确认consumer是否已经正确启动`：
		* 增加一些`健康监测页面`或其他机制以判断consumer是否正确启动。
		* 查看自己`应用日志`以及/data/applogs/tomcat/`catalina.out`日志，确认没有影响应用正常启动的异常出现。
	* `确认topic是否有生产者在持续生产消息`，可以参考[问题1](#q1)，连续查看swallow中的transaction，看是否存在数量变化，如果In:<topic名称>没有变化，说明没有新的消息产生，而不是consumer堵住了。
	* 确认consumer是否在持续消费消息，可以参考[问题2](#q2)，连续查看consumer对应项目的transaction，看MsgConsumed这个type是否数量增加，如果这个数量在增加，说明consumer消费没有堵住。
	* 其次确认`是否该topic其他consumer都在消费，只有自己的consumer停止消费了`。可以参考[问题1](#q1)，查看topic其他consumer的消费情况。
		* __如果该topic其他consumer也都停止消费，且生产者正常工作，`请及时联系swallow团队成员`__。
		* 如果该topic其他consumer消费正常，只有你自己的consumer消费堵住了，请查看consumer对应项目在`CAT`中的`Problem`，找到`Heartbeat`这个type，查询最新的`线程堆栈`，以确认Consumer的线程是否block在onMessage方法内，详细页面请参考下图：
		![Producer Count In CAT](http://code.dianpingoa.com/arch/swallow/raw/master/readme/6.png)
		* 如果consumer的线程block在onMessage方法内，说明onMessage方法内调用存在异常情况，可能原因`包括但不限于``死循环`、`等待IO`、`死锁`、`数据库操作`、`依赖的服务超时`等情况，请仔细检查这些情况，`修复并重启consumer`即可。
		* 如果consumer的线程不存在block现象，`请及时联系swallow团队成员`。
* #### e. 如何确认我的Producer正常工作？
	* 首先确认生产者是否正常启动，判别方法跟[问题4](#q4)中第一点类似，增加检测页面，确保日志中没有影响正常启动的异常出现。
	* 在`CAT`上观察`Producer对应项目`的transaction，找到`MsgProduced`以及`MsgProduceTried`这两个Type，`MsgProduced`的数量表示`程序产生的消息数量`，`MsgProduceTried`表示Swallow的`producer client尝试发送给producer server的次数`，如果这两个数量相差过大，说明存在异常。
	![Producer Count In CAT](http://code.dianpingoa.com/arch/swallow/raw/master/readme/7.png)
	* 正常情况下这两个type的数量是一一对应的，如果设置了重试，在发送失败的情况下，producer会重新尝试发送指定次数，此时MsgProduceTried的数量会大于MsgProduced的数量。如果一段时间内没有新消息发送成功，则可以认为没有新消息产生，或者Producer存在问题，`此时请联系swallow团队成员`。
