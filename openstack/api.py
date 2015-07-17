import keystoneclient.v2_0.client as ksclient
import glanceclient.v2.client as glclient
import heatclient.v1.client as hclient

from credentials import get_keystone_creds
from novaclient import client as novaclient

creds=get_nova_creds()
nova = novaclient.Client("1.1", **creds)

kscreds = get_keystone_creds()
keystone = ksclient.Client(**kscreds)

glance_endpoint = keystone.service_catalog.url_for(service_type='image', endpoint_type='publicURL')
glance = glclient.Client(glance_endpoint, token=keystone.auth_token)

images = glance.images.list()
images.next().name


heat_endpoint = keystone.service_catalog.url_for(service_type='orchestration', endpoint_type='publicURL')
heat = hclient.Client(heat_endpoint,token=keystone.auth_token)