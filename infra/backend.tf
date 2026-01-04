terraform {
  required_version = ">= 1.14.3"

  backend "s3" {
    bucket         = "vanillab-apnortheast2-tfstate"
    key            = "vainillab/terraform/terraform.tfstate"
    region         = "ap-northeast-2"
    encrypt        = true
    dynamodb_table = "terraform-lock"
  }
}