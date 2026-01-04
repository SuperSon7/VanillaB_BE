### global
variable "aws_region" {
  description = "The AWS region to deploy the shard storage layer into"
  default = "ap-northeast-2"
}

variable "assume_role_arn" {
  description = "The role to assume when accessing the AWS API."
  default     = ""
}

### vpc
variable "cidr_numeral" {
  description = "The VPC CIDR numeral (10.x.0.0/16)"
}

variable "availability_zones" {
  type = list(string)
  description = "A comma-delimited list of availability zones for the VPC."
}

variable "cidr_numeral_public" {
  default = {
    "0" = "0"
  }
}

variable "vpc_name" {
  description = "The name of the VPC"
}

variable "cidr_numeral_private" {
  default = {
    "0" = "80"
  }
}

variable "cidr_numeral_private_db" {
  default = {
    "0" = "160"
  }
}