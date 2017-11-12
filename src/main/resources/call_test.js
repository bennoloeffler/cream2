var testFunction = function(name) {
    print('testFunction: Hi there from Javascript, ' + name);
    return "testFunction: greetings from javascript";
};

function testFunction2(name) {
    print('testFunction2: Hi there from Javascript, ' + name);
    return "testFunction2: greetings from javascript";
};

(function() {

function testFunction3(name) {
    print('testFunction3: Hi there from Javascript, ' + name);
    return "testFunction3: greetings from javascript";
};

})();
