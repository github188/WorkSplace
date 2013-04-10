//lihuayu 2005-7-29


struct DescGroupLink : Descriptor<0x08>
{
    U8 position()const { return position_; }
    U32 group_id()const { return n2h32( group_id_ ); } 
private:
    U8 position_;
    U8 group_id_[4];
};
typedef DescList_Iterator<DescGroupLink> GroupLink_I;
