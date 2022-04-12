#!/bin/bash

#This script will auto-install docker-ce version 19

##check service

service='firewalld'

echo "step 1 :check $service service status"

status=$(systemctl status firewalld|grep Active:|awk -F ' ' '{print $2}')

if [ $status != 'Inactive' ];then

echo "stop & disable service $status"

systemctl stop firewalld

systemctl disable firewalld

fi

echo "$service is Inactive, check passed"

echo 'step 2 :check selinux config'

sed -i 's/enforcing/disabled/' /etc/selinux/config

#setenforce 01 临时关闭

echo 'selinux disabled'

echo 'check swap setting'

swapoff -a # 临时关闭

sed -ri 's/.*swap.*/#&/' /etc/fstab #永久关闭

echo 'update host file'

cat > /etc/hosts << EOF

192.168.0.10 k8s-master1

192.168.0.11 k8s-node1

192.168.0.12 k8s-node2

EOF

echo 'update iptable bridge setting'

cat > /etc/sysctl.d/k8s.conf << EOF

net.bridge.bridge-nf-call-ip6tables = 1

net.bridge.bridge-nf-call-iptables = 1

EOF

sysctl --system

#remove old version

yum remove docker \
docker-client \
docker-client-latest \
docker-common \
docker-latest \
docker-latest-logrotate \
docker-logrotate \
docker-engine &> /dev/null

#install dependicies

yum install -y yum-utils &>/dev/null

#update repo to aliyun

yum-config-manager \
--add-repo \
https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo

sed -i 's/download.docker.com/mirrors.aliyun.com\/docker-ce/g' /etc/yum.repos.d/docker-ce.repo

#upate index

yum makecache fast &>/dev/null

#install docker

yum install -y docker-ce-19.03.9 docker-ce-cli-19.03.9 containerd.io &> /dev/null

#update docker image

cat <<EOF | sudo tee /etc/docker/daemon.json

{

 "registry-mirrors": ["https://b9pmyelo.mirror.aliyuncs.com"],

 "exec-opts": ["native.cgroupdriver=systemd"],

 "log-driver": "json-file",

 "log-opts": {

 "max-size": "100m"

 },

 "storage-driver": "overlay2"

}

EOF

#start docker

systemctl daemon-reload

systemctl enable docker

systemctl start docker

#install kubeadm

echo 'Update k8s yum repos'

cat > /etc/yum.repos.d/kubernetes.repo << EOF

[kubernetes]

name=Kubernetes

baseurl=https://mirrors.aliyun.com/kubernetes/yum/repos/kubernetes-el7-x86_64

enabled=1

gpgcheck=0

repo_gpgcheck=0

gpgkey=https://mirrors.aliyun.com/kubernetes/yum/doc/yum-key.gpg https://mirrors.aliyun.com/kubernetes/yum/doc/rpm-package-key.gpg

EOF

echo 'Install kubelet, kubeadm,kubectl'

yum install -y kubelet-1.15.0 kubeadm-1.15.0 kubectl-1.15.0 &> /dev/null

echo 'enable kubelet'

systemctl daemon-reload

systemctl enable kubelet

systemctl start kubelet

