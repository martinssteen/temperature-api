package club.mascotfan.temperature_api.config

import club.mascotfan.temperature_api.MessageHandlerService
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.core.MessageProducer
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory
import org.springframework.integration.mqtt.core.MqttPahoClientFactory
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter
import org.springframework.messaging.MessageHandler

@Configuration
class MqttConfig {

    val log = LoggerFactory.getLogger(MqttConfig::class.java)

    @Bean
    fun mqttClient(
        @Value($$"${mqtt.url}") url: String,
    ): MqttPahoClientFactory {
        return DefaultMqttPahoClientFactory().apply {
            connectionOptions = MqttConnectOptions().apply {
                serverURIs = arrayOf(url)
                isCleanSession = true
            }
        }
    }

    @Bean
    fun inbound(
        clientFactory: MqttPahoClientFactory,
        @Value($$"#{'${mqtt.topics}'.split(',')}") topics: Array<String>,
    ): MessageProducer {
        val adapter = MqttPahoMessageDrivenChannelAdapter(
            "temperature-api-client",
            clientFactory,
            *topics,
        )
        adapter.setOutputChannelName("tedt")
        adapter.setConverter(DefaultPahoMessageConverter())
        adapter.setQos(1)
        return adapter
    }

    @Bean
    @ServiceActivator(inputChannel = "tedt")
    fun handler(messageHandlerService: MessageHandlerService): MessageHandler {
        return MessageHandler { message ->
            val payload: String = message.getPayload().toString()
            val topic: String? = message.headers.get("mqtt_receivedTopic", String::class.java)
            val split = topic?.split("/")
            val source = split?.getOrNull(1) ?: "UNKNOWN"
            val messageContext = split?.last() ?: "UNKNOWN"
            log.trace("Received message from topic: $topic, source: $source, context: $messageContext, payload: $payload")
            when (messageContext) {
                "humidity:0" -> messageHandlerService.handleHumidityMessage(source, payload)
                "temperature:0" -> messageHandlerService.handleTemperatureMessage(source, payload)
                else -> log.trace("ignored message context: $messageContext")
            }
        }
    }
}