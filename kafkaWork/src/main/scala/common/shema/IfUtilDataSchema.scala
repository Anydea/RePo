package common.schema


case class BaseInfo(
  src_id: String,
  own_entity: String,
  ifindex: String,
  ifspeed: Double
) extends schema

case class SampleInfo(
  eventtime: Long,
  num_samples: Int
) extends schema

case class BaseMeasure(
  ingt50: Int,
  ingt75: Int,
  ingt90: Int,
  outgt50: Int,
  outgt75: Int,
  outgt90: Int
) extends schema {
  def +(other: BaseMeasure) = BaseMeasure(this.ingt50+other.ingt50,
        this.ingt75+other.ingt75,
        this.ingt90+other.ingt90,
        this.outgt50+other.outgt50,
        this.outgt50+other.outgt50,
        this.outgt50+other.outgt50)
  
}

case class DetailMeasure(
  maxin:  Double,
  maxout: Double,
  pin:  Double,
  pout: Double,
  avgin: Double,
  avgout: Double,
  maxinerr: Double,
  maxouterr: Double,
  maxindis: Double,
  maxoutdis: Double,
  avginerr: Double,
  avgouterr: Double,
  avgindis: Double,
  avgoutdis: Double,
  maxinoctrate: Double,
  maxoutoctrate: Double,
  pinoctrate: Double,
  poutoctrate: Double,
  avginoctrate: Double,
  avgoutoctrate: Double
) extends schema

case class ifUtilRecord(
  year: Int,
  month: Int,
  day: Int,
  hour: Int,
  minute: Int,
  baseinfo: BaseInfo,
  sampleinfo: SampleInfo,
  basemeasure: BaseMeasure,
  details: DetailMeasure
) extends schema

