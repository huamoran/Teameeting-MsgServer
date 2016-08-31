// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: entity_msg.proto

#import "GPBProtocolBuffers.h"

#if GOOGLE_PROTOBUF_OBJC_GEN_VERSION != 30001
#error This file was generated by a different version of protoc which is incompatible with your Protocol Buffer library sources.
#endif

// @@protoc_insertion_point(imports)

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wdeprecated-declarations"

CF_EXTERN_C_BEGIN

@class ToUser;
GPB_ENUM_FWD_DECLARE(EMsgFlag);
GPB_ENUM_FWD_DECLARE(EMsgHead);
GPB_ENUM_FWD_DECLARE(EMsgTag);
GPB_ENUM_FWD_DECLARE(EMsgType);

NS_ASSUME_NONNULL_BEGIN

#pragma mark - EntityMsgRoot

/// Exposes the extension registry for this file.
///
/// The base class provides:
/// @code
///   + (GPBExtensionRegistry *)extensionRegistry;
/// @endcode
/// which is a @c GPBExtensionRegistry that includes all the extensions defined by
/// this file and all files that it depends on.
@interface EntityMsgRoot : GPBRootObject
@end

#pragma mark - Login

typedef GPB_ENUM(Login_FieldNumber) {
  Login_FieldNumber_UsrFrom = 1,
  Login_FieldNumber_UsrToken = 2,
  Login_FieldNumber_UsrNname = 3,
  Login_FieldNumber_Version = 4,
  Login_FieldNumber_DevType = 5,
  Login_FieldNumber_EnablePush = 6,
};

@interface Login : GPBMessage

/// user id sending from
@property(nonatomic, readwrite, copy, null_resettable) NSString *usrFrom;

/// user token
@property(nonatomic, readwrite, copy, null_resettable) NSString *usrToken;

@property(nonatomic, readwrite, copy, null_resettable) NSString *usrNname;

@property(nonatomic, readwrite, copy, null_resettable) NSString *version;

@property(nonatomic, readwrite) int32_t devType;

@property(nonatomic, readwrite) int32_t enablePush;

@end

#pragma mark - Logout

typedef GPB_ENUM(Logout_FieldNumber) {
  Logout_FieldNumber_UsrFrom = 1,
  Logout_FieldNumber_UsrToken = 2,
  Logout_FieldNumber_Version = 3,
};

@interface Logout : GPBMessage

/// user id sending from
@property(nonatomic, readwrite, copy, null_resettable) NSString *usrFrom;

/// user token
@property(nonatomic, readwrite, copy, null_resettable) NSString *usrToken;

@property(nonatomic, readwrite, copy, null_resettable) NSString *version;

@end

#pragma mark - Keep

typedef GPB_ENUM(Keep_FieldNumber) {
  Keep_FieldNumber_UsrFrom = 1,
  Keep_FieldNumber_Version = 2,
};

@interface Keep : GPBMessage

/// user id sending from
@property(nonatomic, readwrite, copy, null_resettable) NSString *usrFrom;

@property(nonatomic, readwrite, copy, null_resettable) NSString *version;

@end

#pragma mark - Setting

typedef GPB_ENUM(Setting_FieldNumber) {
  Setting_FieldNumber_UsrFrom = 1,
  Setting_FieldNumber_Version = 2,
  Setting_FieldNumber_SetType = 3,
  Setting_FieldNumber_JsonCont = 4,
};

@interface Setting : GPBMessage

/// user id sending from
@property(nonatomic, readwrite, copy, null_resettable) NSString *usrFrom;

@property(nonatomic, readwrite, copy, null_resettable) NSString *version;

/// which one are you setting
@property(nonatomic, readwrite) int64_t setType;

/// key value json format
@property(nonatomic, readwrite, copy, null_resettable) NSString *jsonCont;

@end

#pragma mark - Pushing

typedef GPB_ENUM(Pushing_FieldNumber) {
  Pushing_FieldNumber_Type = 1,
  Pushing_FieldNumber_Content = 2,
};

@interface Pushing : GPBMessage

/// backup
@property(nonatomic, readwrite, copy, null_resettable) NSString *type;

/// push content
@property(nonatomic, readwrite, copy, null_resettable) NSString *content;

@end

#pragma mark - Entity

typedef GPB_ENUM(Entity_FieldNumber) {
  Entity_FieldNumber_MsgHead = 1,
  Entity_FieldNumber_MsgTag = 2,
  Entity_FieldNumber_MsgType = 3,
  Entity_FieldNumber_MsgFlag = 4,
  Entity_FieldNumber_UsrFrom = 5,
  Entity_FieldNumber_MsgCont = 6,
  Entity_FieldNumber_RomId = 7,
  Entity_FieldNumber_RomName = 8,
  Entity_FieldNumber_NckName = 9,
  Entity_FieldNumber_UsrToken = 10,
  Entity_FieldNumber_CmsgId = 11,
  Entity_FieldNumber_Extra = 12,
  Entity_FieldNumber_Version = 13,
  Entity_FieldNumber_Ispush = 14,
  Entity_FieldNumber_MsgTime = 15,
  Entity_FieldNumber_UsrToto = 16,
};

@interface Entity : GPBMessage

/// msg type
@property(nonatomic, readwrite) enum EMsgHead msgHead;

/// msg tag
@property(nonatomic, readwrite) enum EMsgTag msgTag;

/// msg type
@property(nonatomic, readwrite) enum EMsgType msgType;

/// msg flag
@property(nonatomic, readwrite) enum EMsgFlag msgFlag;

/// user id sending from
@property(nonatomic, readwrite, copy, null_resettable) NSString *usrFrom;

/// msg content
@property(nonatomic, readwrite, copy, null_resettable) NSString *msgCont;

/// user room id
@property(nonatomic, readwrite, copy, null_resettable) NSString *romId;

/// user room name
@property(nonatomic, readwrite, copy, null_resettable) NSString *romName;

/// user nick name
@property(nonatomic, readwrite, copy, null_resettable) NSString *nckName;

/// user token
@property(nonatomic, readwrite, copy, null_resettable) NSString *usrToken;

/// client local msgid
@property(nonatomic, readwrite, copy, null_resettable) NSString *cmsgId;

@property(nonatomic, readwrite, copy, null_resettable) NSString *extra;

/// message version
@property(nonatomic, readwrite, copy, null_resettable) NSString *version;

/// if this msg need push when offline
@property(nonatomic, readwrite) int32_t ispush;

/// msg send time
@property(nonatomic, readwrite) uint32_t msgTime;

/// user ids sending to, the string from pms::ToUser
@property(nonatomic, readwrite, strong, null_resettable) ToUser *usrToto;
/// Test to see if @c usrToto has been set.
@property(nonatomic, readwrite) BOOL hasUsrToto;

@end

/// Fetches the raw value of a @c Entity's @c msgHead property, even
/// if the value was not defined by the enum at the time the code was generated.
int32_t Entity_MsgHead_RawValue(Entity *message);
/// Sets the raw value of an @c Entity's @c msgHead property, allowing
/// it to be set to a value that was not defined by the enum at the time the code
/// was generated.
void SetEntity_MsgHead_RawValue(Entity *message, int32_t value);

/// Fetches the raw value of a @c Entity's @c msgTag property, even
/// if the value was not defined by the enum at the time the code was generated.
int32_t Entity_MsgTag_RawValue(Entity *message);
/// Sets the raw value of an @c Entity's @c msgTag property, allowing
/// it to be set to a value that was not defined by the enum at the time the code
/// was generated.
void SetEntity_MsgTag_RawValue(Entity *message, int32_t value);

/// Fetches the raw value of a @c Entity's @c msgType property, even
/// if the value was not defined by the enum at the time the code was generated.
int32_t Entity_MsgType_RawValue(Entity *message);
/// Sets the raw value of an @c Entity's @c msgType property, allowing
/// it to be set to a value that was not defined by the enum at the time the code
/// was generated.
void SetEntity_MsgType_RawValue(Entity *message, int32_t value);

/// Fetches the raw value of a @c Entity's @c msgFlag property, even
/// if the value was not defined by the enum at the time the code was generated.
int32_t Entity_MsgFlag_RawValue(Entity *message);
/// Sets the raw value of an @c Entity's @c msgFlag property, allowing
/// it to be set to a value that was not defined by the enum at the time the code
/// was generated.
void SetEntity_MsgFlag_RawValue(Entity *message, int32_t value);

NS_ASSUME_NONNULL_END

CF_EXTERN_C_END

#pragma clang diagnostic pop

// @@protoc_insertion_point(global_scope)
