//lihuayu 2005-7-29

struct DescCRC32 : Descriptor<0x05>
{
    U32 crc32()const { return n2h32(crc32_); }
private:
    U8 crc32_[4];
};
typedef DescList_Iterator<DescCRC32> CRC32_I;

struct DescModuleLink : Descriptor<0x04>
{
    U8 position()const { return position_; }
    U16 module_id()const { return n2h( module_id_); }
private:
    U8 position_;
    U8 module_id_[2];
};
typedef DescList_Iterator<DescModuleLink> ModuleLink_I;

struct DescCompressModule : Descriptor<0x09>
{
    U8 compression_method()const { return compression_method_; }
    U32 original_size()const { return n2h32( original_size_ ); }
private:
    U8 compression_method_;
    U8 original_size_[4];
};
typedef DescList_Iterator<DescCompressModule> CompressModule_I;
