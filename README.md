# Post-Processing not working if there is a Out Message
###Problem
If the last processor of a route sets a out message, the post processing is not working

###Reproduce
Execute Test: SftpPollEnrichConsumeWithDisconnectAndDeleteIT

The test should move the file "hello.txt" after processing from
<br>target\ftp\SftpPollEnrichConsumeWithDisconnectAndDeleteIT\res\home
<br>to
<br>target\ftp\SftpPollEnrichConsumeWithDisconnectAndDeleteIT\res\home\archive

Change in the test setOutMessage to true (L:71) in order to set a out message in the last processor. Delete the previously created files and folders in target\ftp\SftpPollEnrichConsumeWithDisconnectAndDeleteIT\res\home

Rerun the test: The test fails now and because the post processing was not executed

The post processing is binding the file-properties to the in or out message. So if a out message is set, the properties will be bound to it. However, the evaluation of the Simple Expression for the filename is only working on the in message.
<br>Code Snippets:
<br>Bind Properties: https://github.com/apache/camel/blob/camel-2.24.x/camel-core/src/main/java/org/apache/camel/component/file/strategy/GenericFileRenameProcessStrategy.java#L50
<br>In vs Out Message: https://github.com/apache/camel/blob/camel-2.24.x/camel-core/src/main/java/org/apache/camel/component/file/GenericFile.java#L134
<br>Expression Evaluation (In Message Only): https://github.com/apache/camel/blob/main/core/camel-core-languages/src/main/java/org/apache/camel/language/simple/SimpleExpressionBuilder.java#L259 
