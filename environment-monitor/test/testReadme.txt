gordon@gordon-HP-Notebook:~$ sudo apt install openssh-server
Reading package lists... Done
Building dependency tree
Reading state information... Done
The following package was automatically installed and is no longer required:
  libllvm7
Use 'sudo apt autoremove' to remove it.
The following additional packages will be installed:
  ncurses-term openssh-sftp-server ssh-import-id
Suggested packages:
  molly-guard monkeysphere rssh ssh-askpass
The following NEW packages will be installed
  ncurses-term openssh-server openssh-sftp-server ssh-import-id
0 to upgrade, 4 to newly install, 0 to remove and 25 not to upgrade.
Need to get 637 kB of archives.
After this operation, 5,316 kB of additional disk space will be used.
Do you want to continue? [Y/n] Y
Get:1 http://gb.archive.ubuntu.com/ubuntu bionic-updates/main amd64 ncurses-term all 6.1-1ubuntu1.18.04 [248 kB]
Get:2 http://gb.archive.ubuntu.com/ubuntu bionic-updates/main amd64 openssh-sftp-server amd64 1:7.6p1-4ubuntu0.3 [45.6 kB]
Get:3 http://gb.archive.ubuntu.com/ubuntu bionic-updates/main amd64 openssh-server amd64 1:7.6p1-4ubuntu0.3 [333 kB]
Get:4 http://gb.archive.ubuntu.com/ubuntu bionic-updates/main amd64 ssh-import-id all 5.7-0ubuntu1.1 [10.9 kB]
Fetched 637 kB in 4s (166 kB/s)
Preconfiguring packages ...
Selecting previously unselected package ncurses-term.
(Reading database ... 196181 files and directories currently installed.)
Preparing to unpack .../ncurses-term_6.1-1ubuntu1.18.04_all.deb ...
Unpacking ncurses-term (6.1-1ubuntu1.18.04) ...
Selecting previously unselected package openssh-sftp-server.
Preparing to unpack .../openssh-sftp-server_1%3a7.6p1-4ubuntu0.3_amd64.deb ...
Unpacking openssh-sftp-server (1:7.6p1-4ubuntu0.3) ...
Selecting previously unselected package openssh-server.
Preparing to unpack .../openssh-server_1%3a7.6p1-4ubuntu0.3_amd64.deb ...
Unpacking openssh-server (1:7.6p1-4ubuntu0.3) ...
Selecting previously unselected package ssh-import-id.
Preparing to unpack .../ssh-import-id_5.7-0ubuntu1.1_all.deb ...
Unpacking ssh-import-id (5.7-0ubuntu1.1) ...
Setting up ncurses-term (6.1-1ubuntu1.18.04) ...
Setting up openssh-sftp-server (1:7.6p1-4ubuntu0.3) ...
Setting up ssh-import-id (5.7-0ubuntu1.1) ...
Setting up openssh-server (1:7.6p1-4ubuntu0.3) ...

Creating config file /etc/ssh/sshd_config with new version
Creating SSH2 RSA key; this may take some time ...
2048 SHA256:VAxTUk5jyrLt2mfeCsO5QG7K8fUAcP5ddvaG9p7Hq8U root@gordon-HP-Notebook (RSA)
Creating SSH2 ECDSA key; this may take some time ...
256 SHA256:D5JoWlDi0uaoZaPU4HWAeNcuDJkbTgNxfS0yGKBgxCg root@gordon-HP-Notebook (ECDSA)
Creating SSH2 ED25519 key; this may take some time ...
256 SHA256:Je5GXdo8VGUpFk4uIqrcMlSugjMgquHdLLLLl9bKf1Y root@gordon-HP-Notebook (ED25519)
Created symlink /etc/systemd/system/sshd.service → /lib/systemd/system/ssh.service.
Created symlink /etc/systemd/system/multi-user.target.wants/ssh.service → /lib/systemd/system/ssh.service.
Processing triggers for man-db (2.8.3-2ubuntu0.1) ...
Processing triggers for ufw (0.36-0ubuntu0.18.04.1) ...
Processing triggers for ureadahead (0.100.0-21) ...
ureadahead will be reprofiled on next reboot
Processing triggers for systemd (237-3ubuntu10.38) ...
gordon@gordon-HP-Notebook:~$ sudo systemctl status ssh
● ssh.service - OpenBSD Secure Shell server
   Loaded: loaded (/lib/systemd/system/ssh.service; enabled; vendor preset: enab
   Active: active (running) since Mon 2020-02-24 19:17:46 GMT; 44s ago
 Main PID: 30242 (sshd)
    Tasks: 1 (limit: 4915)
   CGroup: /system.slice/ssh.service
           └─30242 /usr/sbin/sshd -D

Feb 24 19:17:46 gordon-HP-Notebook systemd[1]: Starting OpenBSD Secure Shell ser
Feb 24 19:17:46 gordon-HP-Notebook sshd[30242]: Server listening on 0.0.0.0 port
Feb 24 19:17:46 gordon-HP-Notebook sshd[30242]: Server listening on :: port 22.
Feb 24 19:17:46 gordon-HP-Notebook systemd[1]: Started OpenBSD Secure Shell serv
gordon@gordon-HP-Notebook:~$ sudo ufw allow ssh
Rules updated
Rules updated (v6)
gordon@gordon-HP-Notebook:~$ ssh gordon@gordon-HP-Notebook
The authenticity of host 'gordon-hp-notebook (127.0.1.1)' can't be established.
ECDSA key fingerprint is SHA256:D5JoWlDi0uaoZaPU4HWAeNcuDJkbTgNxfS0yGKBgxCg.
Are you sure you want to continue connecting (yes/no)? y
Please type 'yes' or 'no': yes
Warning: Permanently added 'gordon-hp-notebook' (ECDSA) to the list of known hosts.
gordon@gordon-hp-notebook's password:
Welcome to Ubuntu 18.04.4 LTS (GNU/Linux 5.3.0-40-generic x86_64)

 * Documentation:  https://help.ubuntu.com
 * Management:     https://landscape.canonical.com
 * Support:        https://ubuntu.com/advantage


 * Canonical Livepatch is enabled.
   - Livepatch server check failed.
     Please see /var/log/syslog for more information.

21 packages can be updated.
0 updates are security updates.

Your Hardware Enablement Stack (HWE) is supported until April 2023.

The programs included with the Ubuntu system are free software;
the exact distribution terms for each program are described in the
individual files in /usr/share/doc/*/copyright.

Ubuntu comes with ABSOLUTELY NO WARRANTY, to the extent permitted by
applicable law.

gordon@gordon-HP-Notebook:~$ exit
logout
Connection to gordon-hp-notebook closed.
gordon@gordon-HP-Notebook:~$ man ssh-keygen
gordon@gordon-HP-Notebook:~$
gordon@gordon-HP-Notebook:~$
gordon@gordon-HP-Notebook:~$
gordon@gordon-HP-Notebook:~$ ssh-keygen -t rsa
Generating public/private rsa key pair.
Enter file in which to save the key (/home/gordon/.ssh/id_rsa):
Enter passphrase (empty for no passphrase):
Enter same passphrase again:
Your identification has been saved in /home/gordon/.ssh/id_rsa.
Your public key has been saved in /home/gordon/.ssh/id_rsa.pub.
The key fingerprint is:
SHA256:H1XzuLSAM2rno68/vdaXF4K50DVb3zVmgzGdtBr8SyE gordon@gordon-HP-Notebook
The key's randomart image is:
+---[RSA 2048]----+
|              =..|
|           ..+ *.|
|          + oE*o.|
|         . + *=Oo|
|        S + +.Oo*|
|       . = = o..+|
|          =.....o|
|         ..oo ..o|
|        o+oo.. ..|
+----[SHA256]-----+
gordon@gordon-HP-Notebook:~$


I should have kept notes about this. Did something need to be installed?
We need an ssh server and client and need to enable something.

We also need to add out public key to the authorized_keys

cp .ssh/id_rsa.pub .ssh/authorized_keys cp .ssh/id_rsa.pub .ssh/authorized_keys
