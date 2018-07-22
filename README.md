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
> 1.   You may need to pair Lpms-B before open he app

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
* If you don't know how to parse JSON or unfamiliar with it, please check [this link](http://www.json.org/).

## FAQ
...
