package common.schema


case class BaseInfo4Raw(
  hostname: String,
  owning_entity: String,
  ifindex: String,
  interface_name: String,
  ifspeed: Double
) extends schema

case class DetailMeasure4Raw(
  eventtime: Long,
  input_util: Double,
  output_util: Double,
  in_errors: Int,
  out_errors: Int,
  in_discards: Int,
  out_discards: Int,
  in_octets_rate: Long,
  out_octets_rate: Long
) extends schema

case class ifUtilRawRecord(
    year: Int,
    month: Int,
    day: Int,
    hour: Int,
    minute: Int,
    baseinfo: BaseInfo4Raw,
    details: DetailMeasure4Raw 
) extends schema
