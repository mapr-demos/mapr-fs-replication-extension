

# Setup NFS first!!
# http://doc.mapr.com/display/MapR/Setting+Up+MapR+NFS
# 
# sudo yum install nfs-utils
# sudo yum install mapr-nfs
# sudo usermod -g mapr mapr
# sudo usermod -G users,shadow mapr
#
# ================

sudo showmount -e centos7-sn

sudo mkdir /mapr
echo 'centos7-sn:/mapr   /mapr  nfs   nolock  0  0' | sudo tee -a /etc/fstab

sudo mount /mapr

# incron setup (not needed now)
# ==================
#sudo yum -y install incron
#echo '/mapr/cyber.mapr.cluster IN_ALL_EVENTS java -cp /home/vagrant Test $@ $# $%' | sudo tee /etc/incron.d/replication
#sudo systemctl enable incrond
#sudo systemctl restart incrond
# ==================
# sudo journalctl -u incrond -f &
