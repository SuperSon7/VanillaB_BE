aws_region = "ap-northeast-2"
cidr_numeral = "11"

### VPC

# d after name indicates develop. This means that vanillabd_apnortheast2 VPC is for development environment VPC in Seoul Region.
# but this infra only has production environment VPC so we use just vnaillab_apnortheast2
vpc_name = "vnaillab_apnortheast2"

# Availability Zone list
availability_zones = ["ap-northeast-2a", "ap-northeast-2c"]

# In Seoul Region, some resources are not supported in ap-northeast-2b
availability_zones_without_b = ["ap-northeast-2a", "ap-northeast-2c"]

### EC2

