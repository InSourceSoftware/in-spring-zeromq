# Release Notes

## 0.0.4

* Added gradle-wrapper.properties
* Added publication for Sonatype staging repository
* Updated Spring Boot to 2.3.1.RELEASE
* Added `javax.annotation-api` dependency
* Fixed import of `assertThat` to use `org.hamcrest.MatcherAssert`
* Updated Kotlin to 1.3.72
* Upgraded gradle to 6.4.1

## 0.0.3

* Changed default topic name to `events`
* Un-deprecated `ChannelFactory` and changed purpose of the class
* Refactored `ZmqTemplate` to use `ChannelFactory`
* Renamed `Channels` to `ChannelProxy` and made public
* Made `ZmqHandlerInvoker` an `internal` class
* Improved method signature matching in `ZmqHandlerInvoker`

## 0.0.2

* Added `headers` parameter to `MessageConverter.toMessage()`
* Added `SimpleMessageConverter` class supporting `String` and `ByteArray`
* Removed `DefaultMessageConverter` in favor of `SimpleMessageConverter`
* Added `ZmqTemplate` class with basic `send()` operations
* Deprecated `ChannelFactory` which is superceded by `ZmqTemplate`
* `Channel` now implements `Closeable`

## 0.0.1

* Initial release