## what is ble_rq?(什么是ble_rq?)
ble_rq 全称是 Bluetooh Low Energy request response (低功耗蓝牙请求响应)

它是一个尽可能让你像使用网络请求那样使用蓝牙协议通讯

## readme
ble 通讯有3个channel 读/写/通知 (write/read/notify)

目前我使用的经历都是 写完数据 一定会通过 notify channel 返回数据给我

为了让蓝牙用起来更像是http框架

需要在纯写没有响应的场景下 在模型上自定一个返回数据 框架会将自定义的返回数据返回给业务层
## feature(功能)

目前支持的场景

> write/notify  写/通知 响应模型
> write/read    写/读   响应模型
> write/custom  写/自定义数据结构 蓝牙实际不返回数据 响应模型
> just read     只是读 响应模型
> notify observer  没有产生写的情况下通知监听者

## License(协议)

MIT License

Copyright (c) 2021 Gloomy-潘家骏

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.