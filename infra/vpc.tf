# VPC
# Whole network cider will be 10.0.0.0/8
# A VPC cidr will use the B class with 10.xxx.0.0/16
# You should set cidr advertently because if the number of VPC get larger then the ip range could be in shortage.
resource "aws_vpc" "default" {
  cidr_block = "10.${var.cidr_numeral}.0,0/16"
  enable_dns_hostnames = true
  tags = {
    Name = "vpc-${var.vpc_name}"
  }
}

# Internet Gateway
resource "aws_internet_gateway" "default" {
  vpc_id = aws_vpc.default.id

  tags = {
    Name = "igw-${var.vpc_name}"
  }
}

# NAT Gateway
# Should replace with NAT instance/VPC Endpoint/EIC
resource "aws_nat_gateway" "nat" {
  count = length(var.availability_zones)

  allocation_id = aws_eip.nat.*.id[count.index]

  subnet_id = aws_subnet.public.*.id[count.index]

  lifecycle {
    create_before_destroy = true
  }

  tags = {
    Name = "NAT-GW${count.index}-${var.vpc_name}"
  }

}

# Elastic IP for NAT Gateway
resource "aws_eip" "nat" {
  count = length(var.availability_zones)
  domain = "vpc"

  lifecycle {
    create_before_destroy = true
  }
}

### PUBLIC SUBNETS
# subnet will use cider with /20 -> The number of available IP is 4,096  (Including reserved ip from AWS)\
resource "aws_subnet" "public" {
  count = length(var.availability_zones)
  vpc_id = aws_vpc.default.id

  cidr_block = "10.${var.cidr_numeral}.${var.cidr_numeral_public[count.index]}.0/20"
  availability_zone = var.availability_zones[count.index]

  # Public IP will be assigned automatically when the instance is launch in the public subnet
  map_public_ip_on_launch = true

  tags = {
    Name = "public${count.index}-${var.vpc_name}"
  }
}

# Route Table for public subnets
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.default.id

  tags = {
    Name = "publicrt-${var.vpc_name}"
  }
}

### PRIVATE SUBNETS
# # Subnet will use cidr with /20 -> The number of available IP is 4,096  (Including reserved ip from AWS)
resource "aws_subnet" "private" {
  count = length(var.availability_zones)
  vpc_id = aws_vpc.default.id

  cidr_block = "10.${var.cidr_numeral}.${var.cidr_numeral_private[count.index]}"

  tags = {
    Name = "private${count.index}-${var.vpc_name}"
    Network = "Private"
  }
}

# Route Table for private subnets
resource "aws_route_table" "private" {
  count = length(var.availability_zones)
  vpc_id = aws_vpc.default.id

  tags = {
    Name = "privatert-${var.vpc_name}"
    Network = "Private"
  }
}

# Route Table Association for private subnets
resource "aws_route_table_association" "private" {
  count = length(var.availability_zones)
  subnet_id = aws_subnet.private.*.id[count.index]
  route_table_id = aws_route_table.private.*.id[count.index]
}

# DB PRIVATE SUBNETS
# This subnet is only for the database.
# For security, it is better to assign ip range for database only. This subnet will not use NAT Gateway
# This is also going to use /20 cidr, which might be too many IPs... Please count it carefully and change the cidr.
resource "aws_subnet" "private_db" {
  count = length(var.availability_zones)
  vpc_id = aws_vpc.default.id

  cidr_block = "10.${var.cidr_numeral}.${var.cidr_numeral_private_db[count.index]}.0/20"
  availability_zone = var.availability_zones[count.index]

  tags = {
    Name = "db-private${count.index}-${var.vpc_name}"
    Network = "Private"
  }
}

#Route Table for DB subnets
resource "aws_route_table" "private_db" {
  count = length(var.availability_zones)
  vpc_id = aws_vpc.default.id

  tags = {
    Name = "privatedb${count.index}rt-${var.vpc_name}"
    Network = "Private"
  }
}

# Route Table Association for DB subnets
resource "aws_route_table_association" "private_db" {
  count = length(var.availability_zones)
  subnet_id = aws_subnet.private_db.*.id[count.index]
  route_table_id = aws_route_table.private_db.*.id[count.index]
}