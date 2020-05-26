#shareddict.lua
function get_from_cache(key)
    local cache_ngx = ngx.shared.my_lua_cache
    local value = cache_ngx:get(key)
    return value
end

function set_to_cache(key)
    if not exptime then
        exptime = 0
    end
    local cache_ngx = ngx.shared.my_lua_cache
    local succ, err, forcible = cache_ngx:set(key, value, exptime)
    return succ
end

local args = ngx.req.get_uri_args()
local id = args["id"]
local item_model = get_from_cache("item_"..id)
if item_model == nil then
    local resp = ngx.location.capture("/item/get?id="..id)
    item_model = resp.body
    set_to_cache("item"..id, item_model, 1*60)
end
ngx.say(item_model)




#itemredis.lua
#存在问题
local args = ngx.req.get_uri_args()
local id = args["id"]
local redis = require "resty.redis"
local cache = redis:new()
local ok, err = cache:connect("123.57.204.209", 6379)
local item_model = cache:get("item_"..id)
if item_model == ngx.null or item_model == nil then
    local resp = ngx.location.capture("/item/get?id="..id)
    item_model = resp.body
    #因为resp的ajax已经去访问服务端的逻辑，已经有在redis set缓存，因此不重复执行
    #cache:set("item"..id, item_model, 1*60)
end
ngx.say(item_model)