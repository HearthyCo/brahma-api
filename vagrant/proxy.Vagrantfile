VAGRANTFILE_API_VERSION = "2"

$update_channel = "alpha"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.box = "coreos-%s" % $update_channel
  config.vm.box_version = ">= 500.0.0"
  config.vm.box_url = "http://%s.release.core-os.net/amd64-usr/current/coreos_production_vagrant.json" % $update_channel

  config.vm.provider :virtualbox do |v|
    v.check_guest_additions = false
    v.functional_vboxsf     = false
  end

  config.vm.provision "docker"
  config.vm.provision "shell", inline:
    "ps aux | grep 'sshd:' | awk '{print $2}' | xargs kill"


  # elasticsearch
  config.vm.network :forwarded_port, guest: 9200, host: 9200
  # postgres
  config.vm.network :forwarded_port, guest: 5432, host: 5432
  # rabbitmq
  config.vm.network :forwarded_port, guest: 5672, host: 5672
  config.vm.network :forwarded_port, guest: 15672, host: 15672
  # redis
  config.vm.network :forwarded_port, guest: 6379, host: 6379
  # activator
  config.vm.network :forwarded_port, guest: 8889, host: 8889

  # shared floder
  config.vm.network "private_network", ip: "172.16.14.14"
  #config.vm.synced_folder "../", "/var/docker/brahma", type: "nfs", nfs_version: "3,nolock"
end
