import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  MyHomePage({Key key, this.title}) : super(key: key);

  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  dynamic _message = "";
  static const platform = const MethodChannel('BRAINTREE');

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          children:[
            Text("$_message"),
            Spacer()
            ,
            RaisedButton(onPressed: () async{
              try {
                final dynamic result = await platform.invokeMethod('dropInRequest',{"token":"sandbox_6mjx6zmv_c29sv7p4s56ztvkt"});
                setState(() {
                  _message = result;
                });
              } on PlatformException catch (e) {
                print(e.message);
                setState(() {
                  _message = e.message;
                });
              }
            },child: Text("Buy Something"),)
          ]
        ),
      ),
    );
  }
}
