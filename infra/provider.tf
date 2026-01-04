provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project   = "VaniCoummu"
      ManagedBy = "Terraform"
    }
  }
}

terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.25.0"
    }
  }
}