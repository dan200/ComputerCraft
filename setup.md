you will need to download the following Programs:
IntelliJ
Java 8 (You may need to uninstall Java 9)
Java 8 SDK - You should choose the version that matches your Java
git bash

Run each of those, and wait for them to install.
Next, open git bash and run the following:
git clone you will need to download the following files:
IntelliJ
Java 8 (You may need to uninstall Java 9)
Java 8 SDK - You should choose the version that matches your Java
git bash

Run each of those, and wait for them to install.
Next, open git bash and run the following:
git clone https://github.com/dan200/ComputerCraft.git

When it is done, leave it open, we will use it again.

Open IntelliJ and choose 'import project'. Navigate to the computercraft folder and choose 'build.gradle'
I think it might ask you where your SDK is, if it is 64x it will be in Program Files/Java, labeled jdk1.80_161 (or whatever patch you have)
It will start syncing, when the bottom window says it is done syncing, close IntelliJ.
Now you must type ./setup.bat into git bash
When that finishes, open IntelliJ again.
Goto file>project settings>project. Change the project language level from 9 to 8.
Goto Run>Edit Configs, goto new (+)>Application.
In name, type Forge_Client
In Main Class, type GradleStart
In vm options, type -Xms500m
In module dropdown, choose ComputerCraft
In Jre, choose default (Maybe it was here that it made you choose the JDK? just look at instructions above)
Click Apply, then run
TADA

CC source is in src folder, you can edit it.

When it is done, leave it open, we will use it again.

Open IntelliJ and choose 'import project'. Navigate to the computercraft folder and choose 'build.gradle'
I think it might ask you where your SDK is, if it is 64x it will be in Program Files/Java, labeled jdk1.80_161 (or whatever patch you have)
It will start syncing, when the bottom window says it is done syncing, close IntelliJ.
Now you must type ./setup.bat into git bash
When that finishes, open IntelliJ again.
Goto file>project settings>project. Change the project language level from 9 to 8.
Goto Run>Edit Configs, goto new (+)>Application.
In name, type Forge_Client
In Main Class, type GradleStart
In vm options, type -Xms500m
In module dropdown, choose ComputerCraft
In Jre, choose default (Maybe it was here that it made you choose the JDK? just look at instructions above)
Click Apply, then run
TADA

CC source is in src folder, you can edit it.
