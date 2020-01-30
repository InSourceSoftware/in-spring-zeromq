# Release Notes

## 0.0.1

* Initial release

## 0.0.2

* Added `headers` parameter to `MessageConverter.toMessage()`
* Added `SimpleMessageConverter` class supporting `String` and `ByteArray`
* Removed `DefaultMessageConverter` in favor of `SimpleMessageConverter`
* Added `ZmqTemplate` class with basic `send()` operations
* Deprecated `ChannelFactory` which is superceded by `ZmqTemplate`
* `Channel` now implements `Closeable`