# LpmsB-Android
This is an open-source unofficial Android client for Alubi Lpms-B series.

## What is this
Due to the lower version of the official open source apk, and the official new version of the APK does not provide source code.
So I wrote a new open source demo.

## Usage
> 0.   Android Studio 3.2 or latest
> 1.   Kotlin 1.2.51 or latest
> 2.   Android SDK 28 or latest
> 3.   You Android phone kernal version required 21 or above
> 4.   Android Jetpack 1.0.0-beta3 or latest

## Cautions
> 0.   Please give this app bluetooth permission
> 1.   You may need to pair Lpms-B before open the app

## Flask Server Example

    from flask import Flask
    from flask import request

    app = Flask(__name__)

    @app.route('/save', methods=['POST'])
    def save():
        print (request.form.get('result','OKK'))
        return request.form.get('result','OKK')

    if __name__ == '__main__':
        app.run(port=23456, host='192.168.0.2')

## Response Data Type
* In Android source code, we use Kotlin `MutableList<LpmsData>` to save temp result.
* When the HTTP request is going to launch, we use `GSON.toJson()` function to convert temp result to formatted string.
* If you don't know how to parse JSON or unfamiliar with it, please check [JSON.org](http://www.json.org/).

## Project Dependences
The project dependences are as follows.
* [OKHTTP3](https://github.com/square/okhttp) to replace Android default HTTP client.
* [RxJava2](https://github.com/ReactiveX/RxJava) to make HTTP request and response async.
* [Retrofit2](https://github.com/square/retrofit) to build local HTTP function interface.
* [GraphView](https://github.com/jjoe64/GraphView) to show real-time data in chart.

## JSON Example
* Single LpmsData(X-float,I-integer,L-long)
`{"acc":[X,X,X],"euler":[X,X,X],"frameNumber":I,"gyr":[X,X,X],"imuId":I,"linAcc":[X,X,X],"mag":[X,X,X],"pressure":X,"quat":[X,X,X,X],"timestamp":L}`
* List of LpmsData
`[{Single LpmsData},{Single LpmsData},{Single LpmsData}...]`

## FAQ
...
