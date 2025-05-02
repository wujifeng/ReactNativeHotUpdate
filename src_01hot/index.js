import { Text, NativeModules, DeviceEventEmitter, StyleSheet, View, Button, Alert } from 'react-native'
import React, { Component } from 'react'



export default class index extends Component {

  render() {
    const url = "http://10.56.238.168:80/index.android.bundle";
    const isHermes = () => !!global.HermesInternal;
    const { CalendarModule } = NativeModules;
    const onPress = () => {
      console.log('Reac页面,调用原生模块');
      console.log('./src_01hot/index.js');

      CalendarModule.createCalendarEvent(
        '来自RN的消息',
        'My House',
        (eventId) => {
          console.log(`Created a new event with id ${eventId}`);
        },
      );
      NativeModules.HotUpdate.createUpdateEvent(
        'DIR',
        'My Update',
        (eventId) => {
          console.log(`Created a new event with id ${eventId}`);
        },
      );
    };
    const onPressUpdate = () => {
      console.log('Reac页面,onPressUpdate');
      console.log('./src_01hot/index.js');
      fetch(url, { method: 'GET' })
        .then((response) => {
          if (response.ok) {
            NativeModules.HotUpdate.update(url);
          } else {
            Alert.alert('IP地址网络不通畅或服务器未正确响应，HTTP状态码:' + response.status);
          }
        })
        .catch((error) => {
          Alert.alert('IP地址网络不通畅或服务器未响应:' + error.message);
        });

    };
    DeviceEventEmitter.addListener("onUpdateDownload", onUpdateDownload)
    function onUpdateDownload(...args) {
      console.log(args);
    };

    const onPressTestIP = () => {
      fetch(url, { method: 'GET' })
        .then((response) => {
          if (response.ok) {
            Alert.alert('IP地址通畅，能够建立网络连接。');
          } else {
            Alert.alert('IP地址网络不通畅或服务器未正确响应，HTTP状态码:' + response.status);
          }
        })
        .catch((error) => {
          Alert.alert('IP地址网络不通畅或服务器未响应:' + error.message);
        });
    }

    return (
      <View style={{ width: '100%', flex: 0, alignItems: 'center', justifyContent: 'center', flexDirection: 'column' }}>
        <Text style={{ fontSize: 40 }}>我用于热更新服务测试V266</Text>
        <Text>isHermes：{isHermes() ? "是" : "否"}</Text>
        <Text> </Text>
        <Button title='重启App' onPress={() => onPress()}></Button>
        <Text> </Text>
        <Button title='点击Update' onPress={() => onPressUpdate()}></Button>
        <Text> </Text>
        <Button title='点击测试网络' onPress={() => onPressTestIP()}></Button>
      </View>
    )
  }
}

