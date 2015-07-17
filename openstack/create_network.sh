neutron ext-list

neutron net-create vrrp-net

neutron subnet-create  --name vrrp-subnet --allocation-pool start=10.0.0.2,end=10.0.0.200 vrrp-net 10.0.0.0/24

neutron router-create router1

neutron router-interface-add router1 vrrp-subnet

neutron router-gateway-set router1 public-floating-601

neutron security-group-create vrrp-sec-group

neutron security-group-rule-create  --protocol icmp vrrp-sec-group

neutron security-group-rule-create  --protocol tcp  --port-range-min 80 --port-range-max 80 vrrp-sec-group

neutron security-group-rule-create  --protocol tcp  --port-range-min 22 --port-range-max 22 vrrp-sec-group

nova boot --num-instances 2 --image centos-6.5_x86_64-2015-01-27-v6 --flavor Micro-Small --nic net-id=a655f5b5-5ef3-4f7c-bf38-2ccecd6bdc5b vrrp-node --security_groups vrrp-sec-group

nova list

neutron port-create --fixed-ip ip_address=10.0.0.201 --security-group vrrp-sec-group vrrp-net

neutron floatingip-create --port-id=193f9660-055e-4a0e-9bb4-2bbfc7076dd6 public-floating-601

neutron port-list -- --network_id=a655f5b5-5ef3-4f7c-bf38-2ccecd6bdc5b

neutron port-update 3bacb776-6ccd-48bc-b50a-b10b4a3c3b00 --allowed_address_pairs list=true type=dict ip_address=10.0.0.201

neutron port-update 43702edc-72b9-4428-9bb7-75c637190f5f --allowed_address_pairs list=true type=dict ip_address=10.0.0.201