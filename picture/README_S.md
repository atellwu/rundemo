# Swallow使用说明

[ TOC ]

## 1. Swallow基础概念

* Swallow 是什么:
	* Swallow是一个`基于Topic的异步消息传送系统`。Swallow使用`发布/订阅消息`的传送模型，`消息发布者`指定Topic并发送消息到Swallow消息服务器,`消息订阅者`则指定Topic并从Swallow消息服务器订阅消息。
	* Swallow的发布/订阅模型。消息由Producer发布，ProducerServer负责接收并存储消息到DB。ConsumerServer负责从DB获取消息，并推送给Consumer。
	* Swallow`支持集群订阅者`。在集群中，使用相同ConsumerId(例如Consumer A)的Consumer，将会视作同一个Consumer（同一个Consumer消费的Message将不会重复）。例如，假设一个有2台机器(主机1和主机2)的集群，ConsumerId都是“Consumer-A”，那么`同一则Message，将要么被“主机1”获取，要么被“主机2”获取，不会被两者均获取`。

## 2. swallow名词解释

* Producer表示生产消息的主体，将消息发送到目的地Destination。
* Consumer表示消费消息的主体，从Destination中获取消息。
* Destination表示消息的目的地，也就是消息在swallow中驻留的地方。swallow中定义了两种目的地topic和queue，目前只实现了topic。
* 同步模式表示消息发送成功或者超时才返回。
* 异步模式表示不管消息是否发送成功都立即返回。ASYNC_MODE的时候，生产者发送消息时，先把消费存储到本地文件，另外的线程将文件的消息读取出来发送到server，这种方式调用方的send方法返回的比Sync模式快，但是目前运行情况不是很稳定，有出现丢失消息的情况。所以推荐使用sync模式，sync模式是直接将消息发给server，保证消息能发送成功。
* 消息持久化表示消息会持久化到磁盘或者文件，server重启后消息不会丢失。非持久化与之相反，server重启后消息会丢失。

## 3. Swallow生产者和消费者模拟平台

### 模拟生产者

* 生产者可以向某个topic发送一条消息

	* [dev环境](http://192.168.8.21:7070/rundemo/swallow-dev-067#r=0&j=17)
	* [alpha环境](http://192.168.8.21:7070/rundemo/swallow-alpha-067#r=0&j=17)
	* [qa环境](http://192.168.8.21:7070/rundemo/swallow-qa-067#r=0&j=17)

* (1). 在代码编辑框内，修改“Destination.topic("example")”为“Destination.topic("<你的topic名称>")”)。
* (2). 点击右边绿色run按钮。
* (3). 在右边紫色控制台下方，输入消息的内容，按回车，即可发送。

### 模拟消费者

* 消费者接收消息  (在代码编辑框内，修改“Destination.topic("example")”为“Destination.topic("<你的topic名称>")”)，点击“run”，即可启动消费者。

	* [dev环境](http://192.168.8.21:7070/rundemo/swallow-dev#r=0&j=10  )
	* [qa环境](http://192.168.8.21:7070/rundemo/swallow-qa#r=0&j=10)
	* [alpha环境](http://192.168.8.21:7070/rundemo/swallow-alpha#r=0&j=10 )

## 4. Swallow系统接入流程

### 申请topic

如果有新的topic，联系 `孟文超/宋通`(邮件： wenchao.meng@dianping.com, tong.song@dianping.com )，联系时，请邮件里告知：<br>
    `申请人所在业务部门`： （例如：支付中心业务部门 ）<br>
    `使用swallow解决的业务场景是什么`： （例如：订单支付成功后，使用swallow通知xxx付款成功的消息 ）<br>
    `Topic名称`：   (例如，dp_action)，不能包含点(.)，建议只使用字母和下划线。长度不超过25个字符。<br>
    `生产者业务名，以及负责人姓名`：  (例如，pay-order, 林俊杰)<br>
    `消费者业务名，以及负责人姓名`：  (例如，mobile-api, 陆经天)<br>
    `计划上线时间`<br>
    `每天大概的消息量`：  (例如，5万条 ， 请注意不要写错，比如每日100万消息，应该写“100万”，不要写错成"100") <br>
    `待帮您配置后，方可使用（线下和线上均可以使用），未申请的topic使用时会遇到拒绝连接的异常。`<br>

## 5. Swallow使用说明

* ### 1. 使用swallow发送消息

* #### a. Spring中配置实现

* ##### 1. Maven pox.xml中添加依赖

<pre><code>
&lt;properties>
	&lt;env>dev&lt;/env>
&lt;/properties>		

&lt;dependency>
	&lt;groupId>org.springframework&lt;/groupId>
	&lt;artifactId>spring-beans&lt;/artifactId>
	&lt;version>3.0.5.RELEASE&lt;/version>
&lt;/dependency>
&lt;dependency>
	&lt;groupId>org.springframework&lt;/groupId>
	&lt;artifactId>spring-context&lt;/artifactId>
	&lt;version>3.0.5.RELEASE&lt;/version>
&lt;/dependency>
&lt;dependency>
	&lt;groupId>org.springframework&lt;/groupId>
	&lt;artifactId>spring-core&lt;/artifactId>
	&lt;version>3.0.5.RELEASE&lt;/version>
&lt;/dependency>
&lt;dependency>
	&lt;groupId>com.dianping.swallow&lt;/groupId>
	&lt;artifactId>swallow-producerclient&lt;/artifactId>
	&lt;version>0.6.5&lt;/version> 
&lt;/dependency>
&lt;!-- lion -->
&lt;dependency>
	 &lt;groupId>com.dianping.lion&lt;/groupId>
	 &lt;artifactId>lion-client&lt;/artifactId>
	 &lt;version>0.3.1-SNAPSHOT&lt;/version>
&lt;/dependency>
&lt;dependency>
	 &lt;groupId>com.dianping.lion&lt;/groupId>
	 &lt;artifactId>lion-${env}&lt;/artifactId>
	 &lt;version>1.0.0&lt;/version>
&lt;/dependency>
&lt;!-- 监控 -->
&lt;dependency>
	 &lt;groupId>com.dianping.cat&lt;/groupId>
	 &lt;artifactId>cat-core&lt;/artifactId>
	 &lt;version>0.4.1&lt;/version>
&lt;/dependency>
&lt;dependency>
	 &lt;groupId>com.dianping.hawk&lt;/groupId>
	 &lt;artifactId>hawk-client&lt;/artifactId>
	 &lt;version>0.7.1&lt;/version>
&lt;/dependency>
&lt;!-- 远程调用Pigeon -->
&lt;dependency>
	 &lt;groupId>com.dianping.dpsf&lt;/groupId>
	 &lt;artifactId>dpsf-net&lt;/artifactId>
	 &lt;version>1.9.1-SNAPSHOT&lt;/version>
&lt;/dependency>
</code></pre>

* env可选的值包括dev（开发环境），alpha和beta（都是测试环境）。
* swallow-producerclient的版本可以在[mvn repo](http://mvn.dianpingoa.com/webapp/home.html)查询所有的发行版本。本例中使用0.6.5版本。

* ##### 2. Spring配置文件applicationContext-producer.xml配置相关bean

<pre><code>
&lt;bean id="producerFactory" class="com.dianping.swallow.producer.impl.ProducerFactoryImpl" factory-method="getInstance" />

&lt;bean id="producerClient" factory-bean="producerFactory" factory-method="createProducer">
	&lt;constructor-arg>
		&lt;ref bean="destination" />
	&lt;/constructor-arg>
	&lt;constructor-arg>
		&lt;ref bean="producerConfig" />
	&lt;/constructor-arg>
&lt;/bean>

&lt;bean id="destination" class="com.dianping.swallow.common.message.Destination" factory-method="topic">
	&lt;constructor-arg value="example" />
&lt;/bean>

&lt;bean id="producerConfig" class="com.dianping.swallow.producer.ProducerConfig">
	&lt;property name="mode" value="SYNC_MODE" />
	&lt;property name="syncRetryTimes" value="0" />
	&lt;property name="zipped" value="false" />
	&lt;property name="threadPoolSize" value="5" />
	&lt;property name="sendMsgLeftLastSession" value="false" />
&lt;/bean>
</code></pre>

* ##### 3. Spring代码

<pre><code>
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;
import com.dianping.swallow.producer.Producer;

public class ProducerSpring {
	public static void main(String[] args) {
	ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[] { "applicationContext-producer.xml" });
	Producer producer = (Producer) ctx.getBean("producerClient");
		try {
			System.out.println(producer.sendMessage("Hello world.") + "hello");
		} catch (SendFailedException e) {
			e.printStackTrace();
		}
	}	
}
</code></pre>

* #### b. 生产者端纯代码实现

* 纯代码实现与使用Spring配置bean有一样的效果。

<pre><code>
public class SyncProducerExample{
	public static void main(String[] args) throws Exception {
		producerConfig config = new ProducerConfig();  //(1)
		//以下设置的值与默认配置一致，可以省略
		config.setMode(ProducerMode.SYNC_MODE);  //(2)
		config.setSyncRetryTimes(0);
		config.setZipped(false);
		config.setThreadPoolSize(5);
		config.setSendMsgLeftLastSession(false);

		Producer p = ProducerFactoryImpl.getInstance().createProducer(Destination.topic("example"), config);  //(3)
		for (int i = 0; i &lt; 10; i++) {
			String msg = "消息-" + i;
			p.sendMessage(msg);  //(4)
			System.out.println("Sended msg:" + msg);
			Thread.sleep(500);
		}
	}
}
</code></pre>
 
* (1). 使用swallow发送消息时，首先需要对发送端进行配置，这由ProducerConfig完成。由于ProducerConfig没有提供构造函数，所以只能调用默认构造函数，这样所有属性都会被设置为默认值。下表列出了生产者的所有属性及其默认值。

 	* mode表示producer表示工作模式。
	* asyncRetryTimes表示异步模式下发送失败重试次数。
 	* syncRetryTimes表示同步模式下发送失败重试次数。
 	* zipped表示是否对待发送消息进行压缩。当消息量很大时可以设置此标志，将消息压缩后再发送。
 	* threadPoolSize表示异步模式时，线程池大小。默认值是1，如果设置成多线程，那么会有多个线程同时从FileQueue获取消息并发送，这样的话发送的消息就无法保证其先后顺序。
 	* sendMsgLeftLastSession表示异步模式时，是否重启续传。

<table class="table table-bordered table-striped table-condensed" >
   <tr>
      <td>&#23646;&#24615;</td>
      <td> &#40664;&#35748;&#20540;</td>
   </tr>
   <tr>
      <td>mode </td>
      <td>DEFAULT_PRODUCER_MODE=ProducerMode.ASYNC_MODE</td>
   </tr>
   <tr>
      <td>asyncRetryTimes </td>
      <td>DEFAULT_ASYNC_RETRY_TIMES=10</td>
   </tr>
   <tr>
      <td>syncRetryTimes </td>
      <td>DEFAULT_SYNC_RETRY_TIMES=0</td>
   </tr>
   <tr>
      <td>zipped </td>
      <td>DEFAULT_ZIPPED=false</td>
   </tr>
   <tr>
      <td>threadPoolSize </td>
      <td>DEFAULT_THREADPOOL_SIZE=1</td>
   </tr>
   <tr>
      <td>sendMsgLeftLastSession </td>
      <td>DEFAULT_SEND_MSG_LEFT_LAST_SESSION=true</td>
   </tr>
</table>

* (2). 如果想更改默认设置，则可以调用相应的setter函数进行设置，下图列出了所有可配置属性及其getter和setter函数。生产者共有3中模式，即同步模式ProducerMode.SYNC_MODE,异步模式ProducerMode.ASYNC_MODE和ProducerMode.ASYNC_SEPARATELY_MODE。ASYNC_MODE的时候，生产者发送消息时，先把消费存储到本地文件，另外的线程将文件的消息读取出来发送到server，这种方式调用方的send方法返回的比Sync模式快，但是目前运行情况不是很稳定，有出现丢失消息的情况。所以推荐使用sync模式，sync模式是直接将消息发给server，保证消息能发送成功。
     
<table class= "table table-bordered table-striped table-condensed">
   <tr>
      <td>&#26041;&#27861; </td>
      <td>&#25551;&#36848;</td>
   </tr>
   <tr>
      <td>String getFilequeueBaseDir() </td>
      <td>&#36820;&#22238;&#25991;&#20214;&#38431;&#21015;&#36335;&#24452;</td>
   </tr>
   <tr>
      <td>void setFilequeueBaseDir(String) </td>
      <td>&#35774;&#32622;&#25991;&#20214;&#38431;&#21015;&#36335;&#24452;</td>
   </tr>
   <tr>
      <td>ProducerMode getMode() </td>
      <td>&#36820;&#22238;&#29983;&#20135;&#32773;&#27169;&#24335;</td>
   </tr>
   <tr>
      <td>void setMode(ProducerMode) </td>
      <td>&#35774;&#32622;&#29983;&#20135;&#32773;&#27169;&#24335;</td>
   </tr>
   <tr>
      <td>int getAsyncRetryTimes() </td>
      <td>&#36820;&#22238;&#24322;&#27493;&#28040;&#24687;&#21457;&#36865;&#37325;&#35797;&#27425;&#25968;</td>
   </tr>
   <tr>
      <td>void setAsyncRetryTimes(int) </td>
      <td>&#35774;&#32622;&#24322;&#27493;&#28040;&#24687;&#21457;&#36865;&#37325;&#35797;&#27425;&#25968;</td>
   </tr>
   <tr>
      <td>boolean isZipped() </td>
      <td>&#26159;&#21542;&#23558;&#28040;&#24687;&#21387;&#32553;&#20256;&#36755;</td>
   </tr>
   <tr>
      <td>void setZipped(boolean) </td>
      <td>&#35774;&#32622;&#28040;&#24687;&#21387;&#32553;&#20256;&#36755;</td>
   </tr>
   <tr>
      <td>int getThreadPoolSize() </td>
      <td>&#36820;&#22238;&#24322;&#27493;&#27169;&#24335;&#19979;&#32447;&#31243;&#27744;&#22823;&#23567;</td>
   </tr>
   <tr>
      <td>void setThreadPoolSize() </td>
      <td>&#35774;&#32622;&#24322;&#27493;&#27169;&#24335;&#19979;&#32447;&#31243;&#27744;&#22823;&#23567;</td>
   </tr>
   <tr>
      <td>boolean isSendMsgLeftLastSession() </td>
      <td>&#26159;&#21542;&#23558;&#28040;&#24687;&#26029;&#28857;&#32493;&#20256;</td>
   </tr>
   <tr>
      <td>void setSendMsgLeftLastSession(boolean) </td>
      <td>&#35774;&#32622;&#28040;&#24687;&#26029;&#28857;&#32493;&#20256;</td>
   </tr>
   <tr>
      <td>int getSyncRetryTimes() </td>
      <td>&#36820;&#22238;&#21516;&#27493;&#28040;&#24687;&#21457;&#36865;&#37325;&#35797;&#27425;&#25968;</td>
   </tr>
   <tr>
      <td>void setSyncRetryTimes(int) </td>
      <td>&#35774;&#32622;&#21516;&#27493;&#28040;&#24687;&#21457;&#36865;&#37325;&#35797;&#27425;&#25968;</td>
   </tr>
</table>
     
* (3). 设置好发送端属性后就可以对生产者对象进行构造。ProducerFactoryImpl实现了ProducerFactory，并且其自身为单例对象，调用静态方法getInstance()返回这个单例工厂对象，执行createProducer会返回ProducerImpl实例，而ProducerImpl自身实现了接口Producer。作为生产者，需要绑定消息发送的目的地，Destination实现了对目的地的抽象，其静态方法topic(String name)会返回主题是name的消息目的地。
     
* (4). Producer唯一定义了发送消息的方法sendMessage,下图列出了不同版本的sendMessage。对于需要发送的消息，如果是String类型，则直接发送；如果是其他类型则会被序列化为json字符串进行传输。开发时需要注意：
 
 	* 请确保content对象的类型具有默认构造方法。<br>
 	* 尽量保证content对象是简单的类型(如String/基本类型包装类/POJO)。如果content是复杂的类型，建议在您的项目上线之前，在接收消息端做测试，验证是否能够将content正常反序列化。
 
<table class= "table table-bordered table-striped table-condensed">
   <tr>
      <td>&#26041;&#27861;</td>
      <td>&#25551;&#36848;</td>
   </tr>
   <tr>
      <td>String sendMessage(Object content)</td>
      <td>content&#20026;&#21457;&#36865;&#30340;&#28040;&#24687;</td>
   </tr>
   <tr>
      <td>String sendMessage(Object content,String messageType) </td>
      <td>messageType&#29992;&#20110;&#25351;&#23450;&#36807;&#28388;&#30340;&#28040;&#24687;&#31867;&#22411;</td>
   </tr>
   <tr>
      <td>String sendMessage(Object content, Map<String,String> properties)</td>
      <td>properties&#25351;&#23450;&#28040;&#24687;&#23646;&#24615;</td>
   </tr>
   <tr>
      <td>String sendMessage(Object content, Map<String, String> properties, String messageType)</td>
      <td>&#21516;&#26102;&#25351;&#23450;&#36807;&#28388;&#30340;&#28040;&#24687;&#31867;&#22411;&#21644;&#28040;&#24687;&#23646;&#24615;</td>
   </tr>
</table>


* ### 2. 使用swallow接收消息

* #### a. Spring中配置实现

* ##### 1. Maven pox.xml中添加依赖

<pre><code>
&lt;properties>
	&lt;env>dev&lt;/env>
&lt;/properties>		

&lt;dependency>
	&lt;groupId>org.springframework&lt;/groupId>
	&lt;artifactId>spring-beans&lt;/artifactId>
	&lt;version>3.0.5.RELEASE&lt;/version>
&lt;/dependency>
&lt;dependency>
	&lt;groupId>org.springframework&lt;/groupId>
	&lt;artifactId>spring-context&lt;/artifactId>
	&lt;version>3.0.5.RELEASE&lt;/version>
&lt;/dependency>
&lt;dependency>
	&lt;groupId>org.springframework&lt;/groupId>
	&lt;artifactId>spring-core&lt;/artifactId>
	&lt;version>3.0.5.RELEASE&lt;/version>
&lt;/dependency>
&lt;dependency>
	&lt;groupId>com.dianping.swallow&lt;/groupId>
	&lt;artifactId>swallow-consumerclient&lt;/artifactId>
	&lt;version>0.6.5&lt;/version> 
&lt;/dependency>
&lt;!-- lion -->
&lt;dependency>
	 &lt;groupId>com.dianping.lion&lt;/groupId>
	 &lt;artifactId>lion-client&lt;/artifactId>
	 &lt;version>0.3.1-SNAPSHOT&lt;/version>
&lt;/dependency>
&lt;dependency>
	 &lt;groupId>com.dianping.lion&lt;/groupId>
	 &lt;artifactId>lion-${env}&lt;/artifactId>
	 &lt;version>1.0.0&lt;/version>
&lt;/dependency>
&lt;!-- 监控 -->
&lt;dependency>
	 &lt;groupId>com.dianping.cat&lt;/groupId>
	 &lt;artifactId>cat-core&lt;/artifactId>
	 &lt;version>0.4.1&lt;/version>
&lt;/dependency>
&lt;dependency>
	 &lt;groupId>com.dianping.hawk&lt;/groupId>
	 &lt;artifactId>hawk-client&lt;/artifactId>
	 &lt;version>0.7.1&lt;/version>
&lt;/dependency>
</code></pre>

* env可选的值包括dev（开发环境），alpha和beta（都是测试环境）。
* swallow-consumerclient的版本可以在[mvn repo](http://mvn.dianpingoa.com/webapp/home.html)查询所有的发行版本。本例中使用0.6.5版本。

* ##### 2. Spring配置文件applicationContext-consumer.xml配置相关bean

<pre><code>
&lt;!-- 消费者工厂类 -->
&lt;bean id="consumerFactory" class="com.dianping.swallow.consumer.impl.ConsumerFactoryImpl" factory-method="getInstance" />
&lt;!-- 消费者配置类 -->
&lt;bean id="consumerConfig" class="com.dianping.swallow.consumer.ConsumerConfig">
&lt;/bean>
&lt;!-- 消息的目的地(即Topic) -->
&lt;bean id="dest" class="com.dianping.swallow.common.message.Destination" factory-method="topic">
	&lt;constructor-arg>
		&lt;value>example&lt;/value><!-- example为消息的Topic，需自定义 -->
	&lt;/constructor-arg>
&lt;/bean>
&lt;!-- MessageListener为您实现的消息事件监听器，负责处理接收到的消息 -->
&lt;bean id="messageListener" class="com.dianping.swallow.example.consumer.spring.listener.MessageListenerImpl" />
&lt;!-- 消费者 -->
&lt;bean id="consumerClient" factory-bean="consumerFactory" factory-method="createConsumer" init-method="start" destroy-method="close">
	&lt;constructor-arg>
		&lt;ref bean="dest" />
	&lt;/constructor-arg>
	&lt;constructor-arg>
		&lt;value>xx&lt;/value> <!-- xx为消费者id，需自定义 -->
	&lt;/constructor-arg>
	&lt;constructor-arg>
		&lt;ref bean="consumerConfig" />
	&lt;/constructor-arg>
	&lt;property name="listener">
		&lt;ref local="messageListener" />
	&lt;/property>
&lt;/bean>
</code></pre>

* 消息目的地的值example为消息种类，必须是在服务器白名单中的消息种类才能够连接服务器，否则会拒绝连接。
* messageListener要自己实现，需继承com.dianping.swallow.consumer.MessageListener并实现onMessage方法。下面列出MessageListenerImpl的实现供参考。

<pre><code>
package com.dianping.swallow.example.consumer.spring.listener;

import com.dianping.swallow.common.message.Message;
import com.dianping.swallow.consumer.MessageListener;

public class MessageListenerImpl implements MessageListener {

	@Override
	public void onMessage(Message swallowMessage) {

		System.out.println(swallowMessage.getMessageId() + ":" + swallowMessage.getContent()+ ":" + swallowMessage.getType());
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
</code></pre>

* ##### 3. Spring代码

<pre><code>
package com.dianping.swallow.example.consumer.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.dianping.swallow.consumer.Consumer;

public class TestConsumer {

	public static void main(String[] args) throws InterruptedException {
		ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[] { "applicationContext-consumer.xml" });
		final Consumer consumerClient = (Consumer) ctx.getBean("consumerClient");  
		consumerClient.start();
	}
}
</code></pre>

* #### b. 消费者端纯代码实现

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


* (1). 使用swallow接收消息时，首先需要对接收端进行配置，这由ConsumerConfig完成。由于ConsumerConfig没有提供构造函数，所以只能调用默认构造函数，这样所有属性都会被设置为默认值。下图列出了消费者的所有属性及其默认值。

	* threadPoolSize表示consumer处理消息的线程池线程数，默认为1。 Consumer接收到消息时，会调用用户 实现的MessageListener.onMessage。默认情况下，Consumer内部使用单线程来调用MessageListener.onMessage，即Consumer会单线程地调用onMessage，只有onMessage执行完并响应给服务器（即发送ack给服务器），服务器在收到ack后，才会推送下一个消息过来。如果希望并行地处理更多消息，可以通过设置threadPoolSize，实现多线程（本地有threadPoolSize个线程调用onMessage()，同时服务器也可以在未收到threadPoolSize个ack的情况下继续推送消息），能提高接收消息的速度，但是如此一来，消息的先后顺序则无法保证 。
	* messageFilter表示consumer只消费“Message.type属性包含在指定集合中”的消息。
	* consumerType表示consumer的类型，包括2种类型：
 
		* AT_LEAST：尽量保证消息最少消费一次，不出现消息丢失的情况。(注意：只是尽量保证，而非绝对保证。)
		* NON_DURABLE：临时的消费类型，从当前的消息开始消费，不会对消费状态进行持久化，Server重启后将重新开始。

	* delayBaseOnBackoutMessageException表示当MessageListener.onMessage(Message)抛出BackoutMessageException异常时，2次重试之间最小的停顿时间。
	* delayUpperboundOnBackoutMessageException表示当MessageListener.onMessage(Message)抛出BackoutMessageException异常时，2次重试之间最大的停顿时间。
	* retryCountOnBackoutMessageException表示当MessageListener.onMessage(Message)抛出BackoutMessageException异常时，最多重试的次数。
	* startMessageId表示当需要在建立连接的时候指定读取消息的位置，可以设置该参数指定 。
     
<table  class= "table table-bordered table-striped table-condensed">
   <tr>
      <td>&#23646;&#24615;</td>
      <td>&#40664;&#35748;&#20540;</td>
   </tr>
   <tr>
      <td>threadPoolSize </td>
      <td>1</td>
   </tr>
   <tr>
      <td>messageFilter</td>
      <td>MessageFilter.AllMatchFilter</td>
   </tr>
   <tr>
      <td>consumerType</td>
      <td>ConsumerType.DURABLE_AT_LEAST_ONCE</td>
   </tr>
   <tr>
      <td>delayBaseOnBackoutMessageException</td>
      <td>100ms</td>
   </tr>
   <tr>
      <td>delayUpperboundOnBackoutMessageException</td>
      <td>3000ms</td>
   </tr>
   <tr>
      <td>retryCountOnBackoutMessageException</td>
      <td>5</td>
   </tr>
   <tr>
      <td>startMessageId</td>
      <td>-1</td>
   </tr>
</table>

      
* (2). 如果想更改默认设置，则可以调用相应的setter函数进行设置，下图列出了所有可配置属性及其getter和setter函数。

<table class= "table table-bordered table-striped table-condensed">
   <tr>
      <td>&#26041;&#27861;</td>
      <td>&#25551;&#36848;</td>
   </tr>
   <tr>
      <td>int getThreadPoolSize()</td>
      <td>&#36820;&#22238;consumer&#22788;&#29702;&#28040;&#24687;&#30340;&#32447;&#31243;&#27744;&#32447;&#31243;&#25968;</td>
   </tr>
   <tr>
      <td>void setThreadPoolSize(int)</td>
      <td>&#35774;&#32622;consumer&#22788;&#29702;&#28040;&#24687;&#30340;&#32447;&#31243;&#27744;&#32447;&#31243;&#25968;</td>
   </tr>
   <tr>
      <td>MessageFilter getMessageFilter()</td>
      <td>&#36820;&#22238;&#28040;&#24687;&#36807;&#28388;&#26041;&#24335;</td>
   </tr>
   <tr>
      <td>void setMessageFilter(MessageFilter)</td>
      <td>&#36820;&#22238;&#28040;&#24687;&#36807;&#28388;&#26041;&#24335;</td>
   </tr>
   <tr>
      <td>ConsumerType getConsumerType()</td>
      <td>&#36820;&#22238;&#28040;&#36153;&#32773;&#31867;&#22411;</td>
   </tr>
   <tr>
      <td>void setConsumerType(ConsumerType)</td>
      <td>&#35774;&#32622;&#28040;&#36153;&#32773;&#31867;&#22411;</td>
   </tr>
   <tr>
      <td>int getDelayBaseOnBackoutMessageException()</td>
      <td>&#36820;&#22238;2&#27425;&#37325;&#35797;&#20043;&#38388;&#26368;&#23567;&#30340;&#20572;&#39039;&#26102;&#38388;</td>
   </tr>
   <tr>
      <td>void setDelayBaseOnBackoutMessageException(int)</td>
      <td>&#35774;&#32622;2&#27425;&#37325;&#35797;&#20043;&#38388;&#26368;&#23567;&#30340;&#20572;&#39039;&#26102;&#38388;</td>
   </tr>
   <tr>
      <td> int getDelayUpperboundOnBackoutMessageException()</td>
      <td>&#36820;&#22238;2&#27425;&#37325;&#35797;&#20043;&#38388;&#26368;&#22823;&#30340;&#20572;&#39039;&#26102;&#38388;</td>
   </tr>
   <tr>
      <td>void setDelayUpperboundOnBackoutMessageException(int)</td>
      <td>&#35774;&#32622;2&#27425;&#37325;&#35797;&#20043;&#38388;&#26368;&#22823;&#30340;&#20572;&#39039;&#26102;&#38388;</td>
   </tr>
   <tr>
      <td>int getRetryCountOnBackoutMessageException()</td>
      <td>&#36820;&#22238;&#26368;&#22810;&#37325;&#35797;&#30340;&#27425;&#25968;</td>
   </tr>
   <tr>
      <td>void setRetryCountOnBackoutMessageException(int)</td>
      <td>&#35774;&#32622;&#26368;&#22810;&#37325;&#35797;&#30340;&#27425;&#25968;</td>
   </tr>
   <tr>
      <td>long getStartMessageId()</td>
      <td>&#36820;&#22238;&#35835;&#21462;&#28040;&#24687;&#30340;&#20301;&#32622;</td>
   </tr>
   <tr>
      <td>void setStartMessageId(long)</td>
      <td>&#35774;&#32622;&#35835;&#21462;&#28040;&#24687;&#30340;&#20301;&#32622;</td>
   </tr>
</table>
     
* (3). 设置好接收端属性后就可以对消费者对象进行构造。ConsumerFactoryImpl实现了ConsumerFactory，并且其自身为单例对象，调用静态方法getInstance()返回这个单例工厂对象，执行createConsumer会返回ConsumerImpl实例，而ConsumerImpl自身实现了接口Consumer。作为消费者，需要绑定消息发送的目的地，Destination实现了对目的地的抽象，其静态方法topic(String name)会返回主题是name的消息目的地，该名字需要与所感兴趣的生产者指定的目的地名称一致。
     
* (4). Consumer唯一定义了异步接受消息的方法setListener,该方法需要一个实现MessageListener接口的实例作为参数。MessageListener接口唯一定义了onMessage(Message msg)方法，客户端只需要实现该方法，将消息处理逻辑写入其中即可。

* (5). 调用start()方法启动客户端程序。程序内部会使用Netty框架实现网络通信过程。
     
* * * 

## 5. Swallow常见问题以及处理

* ### a. 如何查看我的消费是否有延迟、延迟多少条消息？
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

* ### b. 如何查看我的Consumer消费一条消息的平均时间？
	* 从[CAT](http://cat.dp/)中查看`Consumer ID对应项目`的Transaction，找到`MsgConsumed`和`MsgConsumeTried`这两个type：
	![Producer Count In CAT](http://code.dianpingoa.com/arch/swallow/raw/master/readme/5.png)
	* `MsgConsumed`表示`consumer server给这个consumer推送的消息数量`，`MsgConsumeTried`表示`consumer尝试消费消息的次数`，如果存在失败重试，则MsgConsumeTried数量可能会比MsgConsumed更多。
	* 右边的三列可以看出`consumer调用onMessage回调函数耗费的最小、最大以及平均时间`，如果consumer消费状况一直良好，突然某个时刻开始有消费延时，可以观察一下这里的平均时间是不是比之前更高，如果平均消费时间比正常情况高出很多，可能会造成消费延时。

* ### c. 我的Consumer有延时，该怎么确认问题所在？
	* 首先观察consumer的`平均消费时间`是否存在异常，如果consumer的平均消费时间`比正常情况高出许多`，说明onMessage回调函数依赖的服务存在问题，可以考虑_最近的代码变更_，或询问_依赖的服务_是否存在故障。
	* 如果consumer的`平均消费时间一直很高`，说明consumer的消费线程数太少，可以考虑`修改配置文件增加消费线程数`，或者`扩容应用增加消费机`。
	* 在cat中观察consumer的problem，`如果swallow相关异常过多，请及时联系swallow团队成员`。
	* 如果consumer的平均消费时间`一直正常、没有发生突变`，则有可能是swallow的consumer server负载较高或存在其他故障，`此时请及时联系swallow团队成员`。

* ### d. 我的Consumer堵了，该怎么确认问题所在？
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
* ### e. 如何确认我的Producer正常工作？
	* 首先确认生产者是否正常启动，判别方法跟[问题4](#q4)中第一点类似，增加检测页面，确保日志中没有影响正常启动的异常出现。
	* 在`CAT`上观察`Producer对应项目`的transaction，找到`MsgProduced`以及`MsgProduceTried`这两个Type，`MsgProduced`的数量表示`程序产生的消息数量`，`MsgProduceTried`表示Swallow的`producer client尝试发送给producer server的次数`，如果这两个数量相差过大，说明存在异常。
	![Producer Count In CAT](http://code.dianpingoa.com/arch/swallow/raw/master/readme/7.png)
	* 正常情况下这两个type的数量是一一对应的，如果设置了重试，在发送失败的情况下，producer会重新尝试发送指定次数，此时MsgProduceTried的数量会大于MsgProduced的数量。如果一段时间内没有新消息发送成功，则可以认为没有新消息产生，或者Producer存在问题，`此时请联系swallow团队成员`。



