fn map(ftn, *lists) {
    lstCount := len(lists);
    if lstCount < 1 {
        throw new IndexError();
    }
    resList := [];
    var loopTimes = len(lists[0]);
    for i := 0; i < loopTimes; i++ {
        args := [];
        for j := 0; j < lstCount; j++ {
            args.append(lists[j][i]);
        }
        resList.append(ftn(*args));
    }
    return resList;
}

contract map(Callable?,
             lambda v -> List?(v) or Array?(v)) -> List?;


fn foldl(ftn, init, *lists) {
    lstCount := len(lists);
    if lstCount < 1 {
        throw new IndexError();
    }
    var loopTimes = len(lists[0]);
    for i := 0; i < loopTimes; i++ {
        args := [];
        for j := 0; j < lstCount; j++ {
            args.append(lists[j][i]);
        }
        init = ftn(*args, init);
    }
    return init;
}

contract foldl(Callable?,
               anyType,
               lambda v -> List?(v) or Array?(v)) -> anyType;


fn all(ftn, lst) {
    for var v in lst {
        if not ftn(v) {
            return false;
        }
    }
    return true;
}

contract all(Callable?, iter?) -> boolean?;


fn any(ftn, lst) {
    for var v in lst {
        if ftn(v) {
            return true;
        }
    }
    return false;
}

contract any(Callable?, iter?) -> boolean?;
