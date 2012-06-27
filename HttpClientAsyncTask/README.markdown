# 如何中断 AsyncTask 中的 HTTP 请求

涉及网络的操作通常会比较耗时，由于不能阻塞 UI 线程，因此这类操作通常会放到单独的线程执行，或是使用 AsyncTask。

AsyncTask enables proper and easy use of the UI thread. This class allows to perform background operations and publish results on the UI thread without having to manipulate threads and/or handlers.

An asynchronous task is defined by a computation that runs on a background thread and whose result is published on the UI thread. An asynchronous task is defined by 3 generic types, called **Params**, **Progress** and **Result**, and 4 steps, called **onPreExecute**, **doInBackground**, **onProgressUpdate** and **onPostExecute**.

## AsyncTask's generic types

The three types used by an asynchronous task are the following:

1. **Params**, the type of the parameters sent to the task upon execution.
2. **Progress**, the type of the progress units published during the background computation.
3. **Result**, the type of the result of the background computation.

Not all types are always used by an asynchronous task. To mark a type as unused, simply use the type `Void`:

    private class MyTask extends AsyncTask<Void, Void, Void> { ... }

## The 4 steps

When an asynchronous task is executed, the task goes through 4 steps:

1. `onPreExecute()`, invoked on the UI thread immediately after the task is executed. This step is normally used to setup the task, for instance by showing a progress bar in the user interface.
2. `doInBackground(Params...)`, invoked on the background thread immediately after `onPreExecute()` finishes executing. This step is used to perform background computation that can take a long time. The parameters of the asynchronous task are passed to this step. The result of the computation must be returned by this step and will be passed back to the last step. This step can also use `publishProgress(Progress...)` to publish one or more units of progress. These values are published on the UI thread, in the `onProgressUpdate(Progress...)` step.
3. `onProgressUpdate(Progress...)`, invoked on the UI thread after a call to `publishProgress(Progress...)`. The timing of the execution is undefined. This method is used to display any form of progress in the user interface while the background computation is still executing. For instance, it can be used to animate a progress bar or show logs in a text field.
4. `onPostExecute(Result)`, invoked on the UI thread after the background computation finishes. The result of the background computation is passed to this step as a parameter.

## Cancelling a task

A task can be cancelled at any time by invoking `cancel(boolean)`. Invoking this method will cause subsequent calls to `isCancelled()` to return true. After invoking this method, `onCancelled(Object)`, instead of `onPostExecute(Object)` will be invoked after `doInBackground(Object[])` returns. To ensure that a task is cancelled as quickly as possible, you should always check the return value of `isCancelled()` periodically from `doInBackground(Object[])`, if possible (inside a loop for instance.)

## Threading rules

There are a few threading rules that must be followed for this class to work properly:

* The task instance must be created on the UI thread.
* `execute(Params...)` must be invoked on the UI thread.
* Do not call `onPreExecute()`, `onPostExecute(Result)`, `doInBackground(Params...)`, `onProgressUpdate(Progress...)` manually.
* The task can be executed only once (an exception will be thrown if a second execution is attempted.)

## Memory observability

AsyncTask guarantees that all callback calls are synchronized in such a way that the following operations are safe without explicit synchronizations.

* Set member fields in the constructor or `onPreExecute()`, and refer to them in `doInBackground(Params...)`.
* Set member fields in `doInBackground(Params...)`, and refer to them in `onProgressUpdate(Progress...)` and `onPostExecute(Result)`.

## 中断 HTTP 请求

通常会把联网的耗时操作放到 `AsyncTask#doInBackground(Params...)` 中执行， 而 **HttpClient** 提供的方法 `HttpClient#execute(HttpUriRequest request)` 是阻塞的。当用户不想等待的时候，如果取消呢？ 参考 [HttpClient Foundamentals](http://hc.apache.org/httpcomponents-client-ga/tutorial/html/fundamentals.html) **HttpClient** 提供了方法 `HttpUriRequest#abort()` 用于中断 HTTP 请求，中断请求之后阻塞的方法会抛出 `IOException` 而返回。因此，我们需要做如下操作

1. 在 AsyncTask 上调用 `cancel (boolean mayInterruptIfRunning)`，丢弃还没有执行的 `AsyncTask`
2. 在进行联网的 `HttpUriRequest` 对象上调用 `abort()`

## 参考
* [AsyncTask API Doc](http://developer.android.com/reference/android/os/AsyncTask.html)
* [Android 中文 API (101) —— AsyncTask](http://www.cnblogs.com/over140/archive/2011/02/17/1956634.html)
* [Android开发中AsyncTask实现异步处理任务的方法](http://mobile.51cto.com/hot-236163.htm)
