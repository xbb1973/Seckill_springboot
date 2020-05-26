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
